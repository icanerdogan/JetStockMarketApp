package com.ibrahimcanerdogan.stockmarket.data.repository

import com.ibrahimcanerdogan.stockmarket.data.mapper.toCompanyInfo
import com.ibrahimcanerdogan.stockmarket.data.parser.CSVParser
import com.ibrahimcanerdogan.stockmarket.data.remote.StockAPI
import com.ibrahimcanerdogan.stockmarket.domain.model.CompanyDetail
import com.ibrahimcanerdogan.stockmarket.domain.model.CompanyList
import com.ibrahimcanerdogan.stockmarket.domain.model.IntradayInfo
import com.ibrahimcanerdogan.stockmarket.domain.repository.StockRepository
import com.ibrahimcanerdogan.stockmarket.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl @Inject constructor(
    private val stockAPI: StockAPI,
    private val companyListingsParser: CSVParser<CompanyList>,
    private val intradayInfoParser: CSVParser<IntradayInfo>,
): StockRepository {

    override suspend fun getCompanyListings(
        query: String
    ): Flow<Resource<List<CompanyList>>> {
        return flow {
            emit(Resource.Loading(true))
            try {
                val response = stockAPI.getListings()
                if (query.isNotEmpty()) {
                    emit(Resource.Success(
                        companyListingsParser.parse(response.byteStream())
                            .filter { it.companyListName.contains(query, ignoreCase = true) }
                    ))
                } else{
                    emit(Resource.Success(companyListingsParser.parse(response.byteStream())))
                }
            } catch(e: IOException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error("Couldn't load data"))
            }
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getIntradayInfo(symbol: String): Resource<List<IntradayInfo>> {
        return try {
            val response = stockAPI.getIntradayInfo(symbol)
            val results = intradayInfoParser.parse(response.byteStream())
            Resource.Success(results)
        } catch(e: IOException) {
            e.printStackTrace()
            Resource.Error(message = "Couldn't load intraday info")
        } catch(e: HttpException) {
            e.printStackTrace()
            Resource.Error(message = "Couldn't load intraday info")
        }
    }

    override suspend fun getCompanyInfo(symbol: String): Resource<CompanyDetail> {
        return try {
            val result = stockAPI.getCompanyInfo(symbol)
            Resource.Success(result.toCompanyInfo())
        } catch(e: IOException) {
            e.printStackTrace()
            Resource.Error(message = "Couldn't load company info")
        } catch(e: HttpException) {
            e.printStackTrace()
            Resource.Error(message = "Couldn't load company info")
        }
    }
}