# Carafe

A timer app designed for pour-over coffee.

Making pour-over coffee is a finicky process. The result of a brew is highly dependent on each variable and anything we can do to control those variables is helpful. Two such variables are time and brew weight. Ensuring that the right amount of water is added at the right time is paramount. The traditional process is to keep your eye on a stopwatch and the scale and do the math in your head for what the weight should read at what time. 

To make this process easier, Carafe does the calculations for you and all you need to do is watch the weight on screen, and keep the weight on the scale in sync. This makes the process easier and more consistent.

## Techincal Design

There are many different techinques for pour over coffee out there, with different steps, different ratios of coffee:water, and different timing between each step. To capture all this variety, I've designed a format for describing arbitrary pour-over coffee recipes and an engine that can "run" these recipes interactively. See[recipes](https://github.com/Vynlar/coffee-re-frame/blob/master/src/cljs/coffee_re_frame/recipe.cljs) for an example of a recipe.

Recipes consist of a series of ordered steps. Each step can have one of several types. Every recipe starts with step of type `:step.type/start` and ends with a step of type `:step.type/end`. There are two additional step types that make the bulk of the recipe: `:step.type/prompt` which waits for user input before continuing. And `:step.type/fixed` which times for a particular amount of time before automatically continuing. With just these 4 step types, you can describe many pour-over recipes.

Additionally, steps can start and stop the global timer by defining the `:step/timer` attribute with the value `:start` or `:stop`.

### Global Step Attributes

- `:step/type` the step type as explained above
- `:step/title` text to display at the top of the page while this step is active
- `:step/description` text to display in the body explaining what to do during this step
- `:step/note` secondary help text (optional)
- `:step/timer` either `:start` or `:stop`. Starts/tops the timer at the _start_ of this step.
- `:step/display` contols what value to display in the main timer window. Either `:step.display/time` or `:step.display/weight`
- `:step/volume` the total expected brew weight at the _end_ of this step's duration

### Fixed Step

Fixed steps can also contain the following properties:
- `:step/duration` the number of seconds for this step to be active before advancing

### Prompt Step

There are no special attributes for this step type.
