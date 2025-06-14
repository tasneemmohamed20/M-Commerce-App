package com.example.m_commerce.data.repository_imp.settings_repo

import com.example.m_commerce.data.datasource.remote.restful.remote.RemoteDataSourceContract
import com.example.m_commerce.domain.repository.IGeoCodingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GeoCodingRepositoryImp @Inject constructor(
    private val remoteDataSource: RemoteDataSourceContract,

    ): IGeoCodingRepository {
    private val geocodingApiKey = "AIzaSyB9cRwZcC2Kirk3Fy2sCEtPUv3zIqRn6Jk"

    override suspend fun getAddressFromGeocoding(
        latlng: String,
        apiKey: String
    ): Flow<String> {
        return remoteDataSource.getAddressFromGeocoding(latlng, geocodingApiKey)
    }
}