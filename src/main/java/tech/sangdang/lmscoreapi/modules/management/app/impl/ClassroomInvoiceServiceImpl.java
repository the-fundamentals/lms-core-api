package tech.sangdang.lmscoreapi.modules.management.app.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sangdang.lmscoreapi.common.exception.ObjectNotFoundException;
import tech.sangdang.lmscoreapi.common.querying.BaseQuery;
import tech.sangdang.lmscoreapi.common.querying.Operators;
import tech.sangdang.lmscoreapi.common.querying.QueryFilterConditions;
import tech.sangdang.lmscoreapi.generated.model.ClassroomInvoiceFilter;
import tech.sangdang.lmscoreapi.generated.model.ClassroomInvoiceResponse;
import tech.sangdang.lmscoreapi.modules.management.app.ClassroomInvoiceService;
import tech.sangdang.lmscoreapi.modules.management.app.mappers.ClassroomInvoiceMapper;
import tech.sangdang.lmscoreapi.modules.management.dom.*;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomInvoiceRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomMemberRepository;
import tech.sangdang.lmscoreapi.modules.management.dom.repository.ClassroomRepository;

@Service
@RequiredArgsConstructor
public class ClassroomInvoiceServiceImpl implements ClassroomInvoiceService {

  private final ClassroomRepository classroomRepository;
  private final ClassroomMemberRepository classroomMemberRepository;
  private final ClassroomInvoiceRepository classroomInvoiceRepository;
  private final ClassroomInvoiceMapper classroomInvoiceMapper;

  @Override
  @Transactional(readOnly = true)
  public List<ClassroomInvoiceResponse> queryInvoices(
      UUID classroomId, ClassroomInvoiceFilter filter) {
    classroomRepository
        .findById(classroomId)
        .orElseThrow(() -> ObjectNotFoundException.of(Classroom.class, classroomId));

    BaseQuery query = classroomInvoiceMapper.toBaseQuery(filter);
    List<QueryFilterConditions> filters =
        query.getFilters() == null ? new ArrayList<>() : new ArrayList<>(query.getFilters());

    // scope query to this classroom; optional memberId comes from the client filter
    filters.add(
        QueryFilterConditions.of(
            ClassroomInvoice.Fields.classroomId, Operators.EQUAL, classroomId.toString()));
    query.setFilters(filters);

    return classroomInvoiceRepository.query(query).map(classroomInvoiceMapper::toResponse).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ClassroomInvoiceResponse getInvoiceById(UUID classroomId, UUID invoiceId) {
    return classroomInvoiceMapper.toResponse(requireInvoiceInClassroom(classroomId, invoiceId));
  }

  @Override
  @Transactional
  public ClassroomInvoiceResponse approveInvoice(UUID classroomId, UUID invoiceId) {
    ClassroomInvoice invoice = requireInvoiceInClassroom(classroomId, invoiceId);
    invoice.approve();
    return classroomInvoiceMapper.toResponse(classroomInvoiceRepository.update(invoice));
  }

  private ClassroomInvoice requireInvoiceInClassroom(UUID classroomId, UUID invoiceId) {
    ClassroomInvoice invoice =
        classroomInvoiceRepository
            .findById(invoiceId)
            .orElseThrow(() -> ObjectNotFoundException.of(ClassroomInvoice.class, invoiceId));

    if (!classroomId.equals(invoice.getClassroomId())) {
      throw ObjectNotFoundException.of(ClassroomInvoice.class, invoiceId);
    }
    return invoice;
  }
}
