package fr.paris.lutece.plugins.identitystore.v3.web.request.validator;

import fr.paris.lutece.plugins.identitystore.business.rules.duplicate.DuplicateRule;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.web.exception.ResourceConsistencyException;

public class DuplicateRuleValidator {
    private static DuplicateRuleValidator instance;

    public static DuplicateRuleValidator instance() {
        if (instance == null) {
            instance = new DuplicateRuleValidator();
        }
        return instance;
    }

    private DuplicateRuleValidator() {
    }

    public void validateActive(final DuplicateRule duplicateRule) throws ResourceConsistencyException {
        if (!duplicateRule.isActive()) {
            throw new ResourceConsistencyException("Duplicate rule is inactive : " + duplicateRule.getCode(),
                                                   Constants.PROPERTY_REST_ERROR_INACTIVE_DUPLICATE_RULE);
        }
    }
}
