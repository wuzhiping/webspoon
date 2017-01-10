# Change Log

## 0.6.1.5 - 2017-01-10

### Added
- Add exit confirmation.
- Add license notices for third-party libraries.
- Take parameters in url to open a file (experimental).
- Set favicon.

### Changed
- Update dependencies to align w/ the official dist.

### Fixed
- Leverage RAP's "Server Push" to trigger UI update.
- Restore Help - About.
- Restore repository export.
- Restore ConditionEditor in FilterRows.
- Fix the hop creation error for multiple streams.

## 0.6.1.4 - 2016-12-21

### Changed
- Rebase to 6.1.0.1-R (6.1.0.1-196).
- Change versioning (this is the 4th patch applied to 6.1).
- Change how to deploy (need the `system/karaf` folder).

### Fixed
- Restore the ability to launch Apache Karaf.
- Restore the marketplace.
- Restore toolTip and helpTip.
- Fix broken unit tests.

## 0.0.0.3 - 2016-11-22
### Fixed
- Restore the missing menubar.
- Revert the type of menuitem from "checkbox" to "push_button" (a fix has been made to pentaho-xul-swt regarding this).
- Fix the issue #3 "Certain keys cannot be typed" by disabling shortcut keys.

## 0.0.0.2 - 2016-11-09
### Fixed
- Fix the main Shell resizing: now the app area aligns with the browser window size.
- Fix the Job icon drawing, hop creation, and logging.
- Fix the <b>Run</b> button: now it works even when another session starts.

## 0.0.0.1 - 2016-11-01
Open-sourced.