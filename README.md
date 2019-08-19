# Gitea SonarQube Plugin

This project is WIP (work in progress), pre-release.
The idea is a `SonarQube` plugin that adds a comment containing the QualityGate metrics to the `Gitea` pull reqeusts,</br>
as well as labeling it as *passed* or *failed*.

## Install

```shell
 # linux
./mvnw package
 # windows
./mvnw.cmd package
```

Will create the `jar` file under the `maven`'s  `target` folder.</br>
Place the created `jar` file in your `SonarQube` plugins folder and restart.

## Configuring

After restarting, the plugin configuration will be added to administration control panel:</br>

![Configuration](pics/plugin-configuration.jpg)

> TIP:</br>
> You can create a designated user in your `Gitea` instance and call it `SonarQube`.</br>
> Generate the token with that user and give it a very strict password (to prevent abuse).</br>
> That will make the comments and labels to be pushed by the `SonarQube` user and not your own.

## How does it work

If configured, the plugin will check if the project's repository is a `Gitea` repository.</br>
If so, tt will push the `QualityGate` result metrics to the pull request conversation as so:</br>

![Comment](pics/plugin-comment.jpg)

With further configuration, the plugin will also label the pull request as so:</br>

![Labels](pics/plugin-labels.jpg)

## Contributing

As stated earlyer, this is a work in progress.</br>
As such, there is tons of work and any contribution will be gladly accepted.</br>
Please check the [CONTRIBUTING.md](CONTRIBUTING.md) before contributing.

## Code of Conduct

Please check the [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before contributing.

## License

[MIT License](LICENSE).
