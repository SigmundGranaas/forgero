The core classes are getting too big, they are doing too much work, and do almost nothing but hold data.
I need to figure out exactly what the system is doing, and split classes into subComponents that can handle this.

Most of the resources in Forgero are pre-registered, but there will also be necessary to create custom combinations and modified resources which are unique and created at runtime.

Assembling/disassembly tools and elements into a composed object:
There are a million different combinations of different elements which can be combined into a tool or a tool part.
The logic behind this is quite importante.
The unique assembly of tools will be a product of how Itemstack data can be gathered.

Rendering stuff:
A full overview of the state of a tool part, but only needing to string to identify correct models.
Need to create a full picture of what the tool is composed of, as well extra information which could affect the looks of tools.

Calculating stats:
Property containers are the only real necessity for calculating the stats of a tool,
state and general information about the tool is not necessary.
Property containers is the only interesting aspect.
These properties will be gathered according to custom ItemStack data.

Presenting useful information about tools:
Accessing raw data and transforming it to make sense and emit useless info. Will need to think about presentation as well.

Creating recipes/upgrades:
Creating recipes for how tools should be crafted, and handling which ingredients are required.

Constructs and slots are vitally important in this section.