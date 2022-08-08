package com.kkt.worthcalculation.model.client

data class Pagination(
    val pageSize: Int = 10,
    val totalPage: Int = 1,
    val totalRows: Int = 1,
    val page: Int = 1
)