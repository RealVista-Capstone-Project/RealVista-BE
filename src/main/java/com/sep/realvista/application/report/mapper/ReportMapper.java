package com.sep.realvista.application.report.mapper;

import com.sep.realvista.application.report.dto.ReportDto;
import com.sep.realvista.domain.report.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    @Mapping(target = "reporterName",
            expression = "java(report.getReporter() != null "
                    + "? report.getReporter().getFullName() : \"Unknown\")")
    @Mapping(target = "reporterEmail",
            expression = "java(report.getReporter() != null "
                    + "? report.getReporter().getEmail().getValue() : \"Unknown\")")
    @Mapping(target = "reportTargetId",
            expression = "java(report.getReportTargetType() == "
                    + "com.sep.realvista.domain.report.ReportTargetType.LISTING "
                    + "? report.getReportedListingId() : report.getReportedUserId())")
    @Mapping(target = "reportedListingName", source = "reportedListing.name")
    @Mapping(target = "reportedUserName",
            expression = "java(report.getReportedUser() != null "
                    + "? report.getReportedUser().getFullName() : null)")
    ReportDto toDto(Report report);
}
