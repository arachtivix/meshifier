# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]
### Added
- JSON schema for mesh data validation
- Mesh validation utilities in `mesh_validator.clj`
- Command-line utility for validating all mesh types
- Documentation for mesh data JSON schema

### Changed
- Updated README.md with validation instructions
- Added `validate-meshes` alias to project.clj

## [0.1.1] - 2025-04-09
### Changed
- Documentation on how to make the widgets.

### Removed
- `make-widget-sync` - we're all async, all the time.

### Fixed
- Fixed widget maker to keep working when daylight savings switches over.

## 0.1.0 - 2025-04-09
### Added
- Files from the new template.
- Widget maker public API - `make-widget-sync`.

[Unreleased]: https://sourcehost.site/your-name/meshifier/compare/0.1.1...HEAD
[0.1.1]: https://sourcehost.site/your-name/meshifier/compare/0.1.0...0.1.1

