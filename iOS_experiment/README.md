# iOS Functionaility Experiments

Generally speaking, iOS development is more restricted when we need to get certain sensor information compared with Android. In this folder, we try to explore if it is possible to achieve certain functionailities or not.

### What have been tested
So far we have verified that the following:
- To get vibration switch status
- To get screen status

Our finding suggests that even though iOS does not directly provide APIs for these values, we can use some other ways to approximate the values.

### Methodology
- Our experiment is based on [SoundSwitch repo](https://github.com/moshegottlieb/SoundSwitch) developed by [moshegottlieb](https://github.com/moshegottlieb). In the repo, it shows that it is possible to detect vibration switch status. We further add the logic to detect screen status. More details can be found in the modified version of `SoundSwitch` folder.
