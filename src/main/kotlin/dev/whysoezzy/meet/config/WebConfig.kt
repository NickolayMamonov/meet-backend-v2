package dev.whysoezzy.meet.config

import dev.whysoezzy.meet.config.storage.StorageProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val storageProperties: StorageProperties
) : WebMvcConfigurer {
    
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Serve uploaded files from local storage
        if (storageProperties.type == dev.whysoezzy.meet.config.storage.StorageType.LOCAL) {
            registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:${storageProperties.local.uploadDir}/")
        }
    }
}
