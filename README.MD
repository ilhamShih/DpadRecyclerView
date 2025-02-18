# DpadRecyclerView

A RecyclerView built for Android TV as a replacement for Leanback's BaseGridView.

See the project website for more information: https://rubensousa.github.io/DpadRecyclerView

Motivation for this library: https://rubensousa.com/2022/11/08/dpadrecyclerview/

Check the sample app for a complete example of integration of this library:

![sample](https://github.com/rubensousa/DpadRecyclerView/blob/master/assets/sample_cover.png?raw=true)

## Getting started

Add the following dependency to your app's `build.gradle`:

```groovy
implementation "com.rubensousa.dpadrecyclerview:dpadrecyclerview:$latestVersion"

// Optional: If you want to use Compose together with DpadRecyclerView
implementation "com.rubensousa.dpadrecyclerview:dpadrecyclerview-compose:$latestVersion"

// Optional: Espresso test helpers for your instrumented tests:
androidTestImplementation "com.rubensousa.dpadrecyclerview:dpadrecyclerview-testing:$latestVersion"
```

Check the official website for more information and recipes: https://rubensousa.github.io/DpadRecyclerView

## New Features compared to Leanback's `BaseGridView`

### Layout

- Supports grids with different span sizes
- Supports reverse layout
- XML attributes for easier configuration

### Scrolling and focus

- Supports changing the alignment configuration smoothly
- Supports limiting the number of pending alignments
- Supports non smooth scroll changes
- Supports continuous and circular grid focus

## Sample app

Nested lists:

![nested_lists](https://github.com/rubensousa/DpadRecyclerView/blob/master/assets/sample_nested_lists.png?raw=true)

[focus_overlay.webm](https://user-images.githubusercontent.com/10662096/219993088-c292de23-0c65-48fb-bde0-ea5ddf9b3bd4.webm)

Grid with different span sizes:

![grid](https://github.com/rubensousa/DpadRecyclerView/blob/master/assets/sample_grid.png?raw=true)


## License

    Copyright 2023 Rúben Sousa
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

