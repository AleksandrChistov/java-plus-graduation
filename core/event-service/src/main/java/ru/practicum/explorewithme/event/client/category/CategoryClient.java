package ru.practicum.explorewithme.event.client.category;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.explorewithme.api.category.service.CategoryServiceApi;
import ru.practicum.explorewithme.event.config.CategoryClientConfig;

@FeignClient(name = "category-service", configuration = CategoryClientConfig.class, fallback = CategoryClientFallback.class)
public interface CategoryClient extends CategoryServiceApi {
}
