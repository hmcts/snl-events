package uk.gov.hmcts.reform.sandl.snlevents.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import java.util.Map;

//configuration meant to be used only in tests, maybe there's better way to do that
public class JpaTestConfiguration {
    @Bean
    @Qualifier(value = "entityManager")
    public EntityManager entityManager(EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.createEntityManager();
    }

    @Bean
    @Qualifier(value = "entityManagerFactory")
    public EntityManagerFactory entityManagerFactory() {
        return new EntityManagerFactory() {
            @Override
            public EntityManager createEntityManager() {
                return null;
            }

            @Override
            public EntityManager createEntityManager(Map map) {
                return null;
            }

            @Override
            public EntityManager createEntityManager(SynchronizationType synchronizationType) {
                return null;
            }

            @Override
            public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
                return null;
            }

            @Override
            public CriteriaBuilder getCriteriaBuilder() {
                return null;
            }

            @Override
            public Metamodel getMetamodel() {
                return null;
            }

            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public void close() {

            }

            @Override
            public Map<String, Object> getProperties() {
                return null;
            }

            @Override
            public Cache getCache() {
                return null;
            }

            @Override
            public PersistenceUnitUtil getPersistenceUnitUtil() {
                return null;
            }

            @Override
            public void addNamedQuery(String name, Query query) {

            }

            @Override
            public <T> T unwrap(Class<T> cls) {
                return null;
            }

            @Override
            public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {

            }
        };
    }
}