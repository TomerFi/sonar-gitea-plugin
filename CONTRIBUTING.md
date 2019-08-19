# Contributing to `sonar-gitea-plugin`

:clap: First off, thank you for taking the time to contribute. :clap:

Contributing is pretty straight-forward:

- Fork the repository
- Commit your changes
- Create a pull request against the `dev` branch

Please feel free to contribute, even to this contributing guideline file, if you see fit.

## Work-In-Progress

This is a work on progress inline for some major refactoring:

- Better test cases: split tests to smaller parts.
- Code re-design: this is very much needed as this code was written on-the-fly with no pre-designing.</br>
  *Hint*: Some api calls can be skipped by creating a couple of new data classes.
- Integration tests: I've only tested this plugin in the following environment:</br>
  `Gitea private instance` -> `Drone private instasnce` -> `SonarQube private instance`.</br>
  If you're working with a differrent CI the `Drone`, It'll be nice if you could perhaps confirm this plugin.

## First Timers

[First timers](https://www.firsttimersonly.com/) are very much welcome here.</br>
As the saying goes, there's no such thing as a *bad PR*, just a *might-need-some-adjustments PR*.</br>
:stuck_out_tongue_winking_eye:

## Code of Conduct

Please check the [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before contributing.
