# Scopes
Scopes provide a mechanism to resolve components in different stages and to make sure that entries have access to the data it needs to evaluate if it should be applied or discarded.

## Goals
* Easy caching of partially resolved entries.
* Define explicit dependencies for entries to be resolved.
* Make it possible to see unresolved entries
* Easily provide a way to disqualify entries.

## Usage
The intended usage of the scope system is to provide a consistent mechanism for the accumulation and resolution of attributes from complex structured components.

In cases where we are interested in using the same item to provide many different attributes depending on where it is used, we need a system that can provide each attribute with the correct data to decide if it is appropriate to be used in this context.

### Examples
Using Oak wood as an example.
* Combined with a shape, we want to only apply composite attributes and combine them with the shape's composite attributes.
* When inserted into a slot, we only want to use the attributes appropriate for this slot.
* When viewed alone we want to be able to look at its attributes in isolation
* Oak wood might have special properties that should only be applied when it is used against undead. Only effective when used as part of the head of a tool and/or when used in an offensive slot.
* When we are looking at the wood material we want to see which attributes will be applied in different scenarios



