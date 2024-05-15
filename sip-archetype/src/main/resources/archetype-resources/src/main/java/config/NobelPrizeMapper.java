package ${package}.config;

import ${package}.scenarios.models.common.Translation;
import ${package}.scenarios.models.laureate.Laureate;
import ${package}.scenarios.models.nobelprize.NobelPrize;
import ${package}.scenarios.models.response.NobelPrizeResponse;
import ${package}.scenarios.models.response.LaureateInfo;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

/**
 * Mapper used to map the collected data into a final response model
 */
@Component
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NobelPrizeMapper {
    NobelPrizeResponse toNobelPrizeResponse(NobelPrize nobelPrize);

    List<LaureateInfo> toLaureates(List<Laureate> laureate);

    default String toEnglish(Translation value) {
        if (value == null) return null;
        return value.getEn();
    }
}