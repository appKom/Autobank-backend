package com.example.autobank.config

import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.cache.configuration.MutableConfiguration
import javax.cache.expiry.CreatedExpiryPolicy
import javax.cache.expiry.Duration

@Configuration
@EnableCaching // You still need this
class CacheConfig {

    @Bean
    fun jCacheManagerCustomizer(): JCacheManagerCustomizer {
        return JCacheManagerCustomizer { cacheManager ->
            // This is where we manually create the "ip-buckets" cache
            // that Bucket4j is looking for.
            cacheManager.createCache("ip-buckets", jCacheConfiguration())
        }
    }

    private fun jCacheConfiguration(): javax.cache.configuration.Configuration<Any, Any> {
        return MutableConfiguration<Any, Any>()
            .setTypes(Any::class.java, Any::class.java)
            .setStoreByValue(false)
            // Example: Make cache entries expire after 1 hour
            .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_HOUR))
    }
}