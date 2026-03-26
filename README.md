# OreSurvey

A Minecraft mod that counts the number of each ore in the player's current area and saves the report in tab-separated values (TSV) format. This mod requires Fabric.

## Usage

This mod works on the client.

The default hotkeys are as follows:

- KEY_LBRACKET - Counts the quantity of each ore type within a certain range
- KEY_BACKSLASH - Saves the count of each ore in TSV format

Each press of the count-up key adds to the count of each ore until the save key is pressed. This is useful for combining results from multiple sample locations.

The surveyed range is between the following two coordinates:

- `(PlayerPosX - 64, -63, PlayerPosZ - 64)`
- `(PlayerPosX + 63, 240, PlayerPosZ + 63)`

The output TSV file will be saved in `<instance_dir>/oresurvey`.

Here is an example of an output TSV file: [20240605_205633_751.tsv](./docs/oresurvey/20240605_205633_751.tsv).

## License

This project is licensed under the MIT license.
