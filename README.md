# mike

Tool for binarization of the biological microscopy image(s).
This application separates the pixel of the cells from background.

## Installation

### For Windows/Linux

Download archived file from [releases page](https://github.com/yori7/mike/releases) and decompress it.

### Manual build

Clone this repository and use `gradle build` command to build it.
(or you can use `.\gradlew build` command instead.)

```bash
git clone https://github.com/yori7/mike && cd mike
gradle build
```

## Usage

1. Press **"Open"** button to open the image to be processed.
2. Put **checkmark(s)** you want to do. (Flat Field is for fixing biased light distribution)
3. Press **"Apply"** button to process.
4. Press **"Save"** button to save processed image.

## Alternative

CUI version made by python is available.
[nmeranoleuca](https://github.com/yori7/nmelanoleuca)
