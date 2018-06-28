package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.VersionedEntity;

import javax.persistence.OptimisticLockException;

@Service
public class VersionCheckService {
    /**
     *
     * @param entityToCheck - Entity with version to be checked
     * @param versionToCompare - reference version number
     * @throws NullPointerException - If there is null entityToCheck or version in it
     * @throws OptimisticLockException - If versionToCompare doesn't match entityVersion
     */
    public void checkVersion(VersionedEntity entityToCheck, long versionToCompare) {
        if (!entityToCheck.getVersion().equals(versionToCompare)) {
            throw new OptimisticLockException();
        }
    }
}
