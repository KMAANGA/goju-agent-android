package com.maangatech.gojuagent.core.database.repository

import com.maangatech.gojuagent.core.database.dao.CustomerDao
import com.maangatech.gojuagent.core.database.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface CustomerRepository {
    fun observeFavorites(): Flow<List<CustomerEntity>>
    fun observeRecent(limit: Int = 20): Flow<List<CustomerEntity>>
    fun search(query: String): Flow<List<CustomerEntity>>
    suspend fun findByMsisdn(msisdn: String): CustomerEntity?
    suspend fun setFavorite(customer: CustomerEntity, favorite: Boolean)
    suspend fun updateDetails(customer: CustomerEntity, nickname: String?, notes: String?)
}

@Singleton
class DefaultCustomerRepository @Inject constructor(
    private val customerDao: CustomerDao,
) : CustomerRepository {

    override fun observeFavorites(): Flow<List<CustomerEntity>> = customerDao.observeFavorites()

    override fun observeRecent(limit: Int): Flow<List<CustomerEntity>> = customerDao.observeRecent(limit)

    override fun search(query: String): Flow<List<CustomerEntity>> = customerDao.search(query)

    override suspend fun findByMsisdn(msisdn: String): CustomerEntity? = customerDao.findByMsisdn(msisdn)

    override suspend fun setFavorite(customer: CustomerEntity, favorite: Boolean) {
        customerDao.update(customer.copy(isFavorite = favorite))
    }

    override suspend fun updateDetails(customer: CustomerEntity, nickname: String?, notes: String?) {
        customerDao.update(customer.copy(nickname = nickname, notes = notes))
    }
}
