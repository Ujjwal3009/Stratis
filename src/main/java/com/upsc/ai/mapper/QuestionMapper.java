package com.upsc.ai.mapper;

import com.upsc.ai.dto.QuestionDTO;
import com.upsc.ai.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    QuestionMapper INSTANCE = Mappers.getMapper(QuestionMapper.class);

    @Mapping(target = "subject", source = "subject.name")
    @Mapping(target = "topic", source = "topic.name")
    @Mapping(target = "options", source = "options")
    QuestionDTO toDto(Question entity);

    @Mapping(target = "text", source = "optionText")
    @Mapping(target = "order", source = "optionOrder")
    QuestionDTO.QuestionOptionDTO toOptionDto(com.upsc.ai.entity.QuestionOption option);
}
