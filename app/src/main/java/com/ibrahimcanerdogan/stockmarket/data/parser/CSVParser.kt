package com.ibrahimcanerdogan.stockmarket.data.parser

import java.io.InputStream

interface CSVParser<T> {
    suspend fun parse(stream: InputStream): List<T>
}