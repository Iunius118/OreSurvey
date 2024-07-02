# OreSurvey

A Minecraft mod for FabricMC that counts the number of ores in the area where the player is located and saves the report in the tab-separated values (TSV) format.

## Usage

This mod works on the client.

The default hotkeys are as follows:

- KEY_LBRACKET - Count up the number of ores in the area
- KEY_BACKSLASH - Save the number of ores in the TSV format

The count-up key adds the number of ores each time it is pressed until the save key is pressed. This is useful for summing the results of multiple sample locations.

The surveyed range is between the following two coordinates:

- `(PlayerPosX - 64, -63, PlayerPosZ - 64)`
- `(PlayerPosX + 63, 240, PlayerPosZ + 63)`

The output TSV file will be saved in `<instance_dir>/oresurvey`.

Here is an example of an output TSV file: [20240605_205633_751.tsv](./docs/oresurvey/20240605_205633_751.tsv).

## License

This project is licensed under the MIT license.
