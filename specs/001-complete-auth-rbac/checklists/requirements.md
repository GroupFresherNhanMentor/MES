# Requirements Checklist

- [X] Login and refresh response is explicitly defined.
- [X] Refresh JWT is stateless and purpose-validated.
- [X] User active state and roles reload on every protected request.
- [X] Multiple roles are supported.
- [X] Every auth/user/role management operation has an explicit ADMIN rule.
- [X] User and role management behavior is specified.
- [X] Final-active-ADMIN safety is specified.
- [X] Permission tables and existing data are preserved but excluded from runtime authorization.
- [X] Unit, integration, contract and migration tests are required.
- [X] SRS, API reference and executable controller policy agree.
