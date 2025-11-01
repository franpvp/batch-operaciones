package com.example.bff_operaciones.repository.impl;

import com.example.bff_operaciones.dto.TradeFilter;
import com.example.bff_operaciones.entity.TradeEntity;
import com.example.bff_operaciones.repository.TradeRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Repository
@Slf4j
public class TradeRepositoryCustomImpl implements TradeRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<TradeEntity> searchByFilter(TradeFilter filter, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // SELECT
        CriteriaQuery<TradeEntity> cq = cb.createQuery(TradeEntity.class);
        Root<TradeEntity> root = cq.from(TradeEntity.class);
        List<Predicate> preds = buildPredicates(filter, cb, root);

        if (log.isDebugEnabled()) {
            log.debug("Predicados: idTrade={}, canal={}, fechaDesde={}, fechaHasta={}",
                    filter != null ? filter.getIdTrade() : null,
                    filter != null ? filter.getCanal() : null,
                    filter != null ? filter.getFechaDesde() : null,
                    filter != null ? filter.getFechaHasta() : null
            );
        }

        cq.select(root).where(preds.toArray(new Predicate[0]));

        // orden
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(s -> {
                Path<?> p = root.get(s.getProperty());
                orders.add(s.isAscending() ? cb.asc(p) : cb.desc(p));
            });
            cq.orderBy(orders);
        } else {
            cq.orderBy(cb.desc(root.get("fechaCreacion")));
        }

        TypedQuery<TradeEntity> tq = em.createQuery(cq);
        tq.setFirstResult((int) pageable.getOffset());
        tq.setMaxResults(pageable.getPageSize());
        List<TradeEntity> content = tq.getResultList();

        // COUNT
        CriteriaQuery<Long> cqc = cb.createQuery(Long.class);
        Root<TradeEntity> countRoot = cqc.from(TradeEntity.class);
        cqc.select(cb.count(countRoot))
                .where(buildPredicates(filter, cb, countRoot).toArray(new Predicate[0]));
        Long total = em.createQuery(cqc).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    private List<Predicate> buildPredicates(TradeFilter f, CriteriaBuilder cb, Root<TradeEntity> root) {
        List<Predicate> ps = new ArrayList<>();
        if (f == null) return ps;

        // idTrade (igual exacto)
        if (f.getIdTrade() != null) {
            // cast explícito por si hay metadatos raros de tipo
            ps.add(cb.equal(root.get("idTrade").as(Long.class), f.getIdTrade()));
        }

        // canal (equals case-insensitive)
        if (f.getCanal() != null && !f.getCanal().trim().isEmpty()) {
            ps.add(cb.equal(cb.upper(root.get("canal")), f.getCanal().trim().toUpperCase()));
        }

        // fechas (incluyentes) sobre fechaCreacion
        if (f.getFechaDesde() != null) {
            ps.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), f.getFechaDesde()));
        }
        if (f.getFechaHasta() != null) {
            ps.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), f.getFechaHasta()));
        }

        return ps;
    }
}
