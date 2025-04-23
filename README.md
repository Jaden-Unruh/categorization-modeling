# Categorization Modeling Project

`org.j3lsmp.categorizationmodeling`

University of Washington; Department of Philosophy

Jaden Unruh, Dr. Conor Mayo-Wilson

## `setup.QuineMcCluskey.java`
This file contains an implementation of the Quine-McCluskey algorithm for minimizing boolean expressions into a simplified disjunctive normal form using prime implicants. A full explanation can be found on [Wikipedia](https://en.wikipedia.org/wiki/Quine%E2%80%93McCluskey_algorithm).

## `Model.java`
This model keeps track of relative 'weights' for each possible hypothesis, where a hypothesis is a truth table mapping states of each input variable to a single output (i.e., the color, leg width, and spottiness of an alien mapping to whether it is evil). For four variables, we have truth tables of length 16 and 65,536 possible hypotheses. For each, we consider its weight to be a number between 0 and 1 modeling an agent's perception of the likelihood that this hypothesis is correct - the sum of all hypothesis weights will always be 1.

We initialize these hypotheses before the agent receives any additional data with a consideration to the simplicity of the hypothesis. We use `setup.QuineMcCluskey.java` to do this, converting each possible truth table to simplified disjunctive normal form and counting both how many prime implicants it has (`n`, anywhere from 0, for all-false and all-true tables, to 7 or 8, e.g. `0111001111100111` which has `[00-0, -00-, 0-10, --01, 011-, 1-0-, -110]`), as well as the floor of the average number of dashes in each of those prime implicants (`m` between 0 and 4). We compute a product of the nth index of `{0, 0.2, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1}`, and the mth index of `{1, .8, .6, .4, .2}`. We subtract this 'complexity' from 1 to have a measure of simplicity, and these simplicities are then normalized to ensure that we add up to 1 for all hypotheses, and this forms our initial weights for each hypothesis.

### Updating the model
Whenever the user receives new information (i.e. is told that a particular 'Alien' is 'good' or 'evil'), we update the model accordingly. Every model that disagrees with the new information has its weight multiplied by the constant `CONFIDENCE_FACTOR`, currently set to 0.1. Those that agree are unchanged, until we again normalize all the weights to ensure they still add to 0.1 - effectively, increasing the weights of those that agree and decreasing those that disagree.

### Making a prediction
Whenever we want to make a prediction about how the agent would respond to a particular 'alien', we iterate through each hypothesis, and, if that hypothesis would determine that this particular 'alien' is 'evil', we add its weight to a running total. After all hypotheses, we have a number between 0 and 1 that represents this model's confidence that the given 'alien' is 'evil'.

### Testing
This model was tested against data from [Kristian Tylén et al](https://doi.org/10.1111/cogs.13338), updating the model for each decision the test subjects made and predicting their next decision. In a preliminary test, the model correctly predicted the decision made in 13,196 of 16,224 trials, about 81.3%.
