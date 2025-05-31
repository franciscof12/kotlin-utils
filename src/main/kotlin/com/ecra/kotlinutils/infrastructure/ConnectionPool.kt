package com.ecra.kotlinutils.infrastructure

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.use

data class Configuration(val jdbcUrl: String, val username: String, val password: String)

data class Parameter(val statementParameter: (Int, PreparedStatement) -> Unit)

class ConnectionPool(configuration: Configuration) {
    private val dataSoure = HikariDataSource(HikariConfig().apply {
        jdbcUrl = configuration.jdbcUrl
        username = configuration.username
        password = configuration.password
        addDataSourceProperty("cachePrepStmts", "true")
        addDataSourceProperty("prepStmtCacheSize", "250")
        addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        addDataSourceProperty("useServerPrepStmts", "true")
        addDataSourceProperty("useLocalSessionState", "true")
        addDataSourceProperty("rewriteBatchedStatements", "true")
        addDataSourceProperty("cacheResultSetMetadata", "true")
        addDataSourceProperty("elideSetAutoCommits", "true")
        addDataSourceProperty("maintainTimeStats", "false")
        addDataSourceProperty("zeroDateTimeBehavior", "convertToNull")
    })

    fun execute(sql: () -> String) =
        dataSoure.connection.use {
            it.prepareStatement(sql()).use {
                it.execute()
            }
        }

    fun <T> executeAndMap(sql: String, params: List<Parameter> = emptyList(), mapper: (ResultSet) -> T) =
        mutableListOf<T>().apply {
            dataSoure.connection.use {
                it.prepareStatement(sql).use { statement ->
                    params.forEachIndexed { index, parameter ->
                        parameter.statementParameter(index + 1, statement)
                    }
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            add(mapper(resultSet))
                        }
                    }
                }
            }
        }.toList()
}