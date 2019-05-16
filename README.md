# Lime

A 2D Game engine, intended for personal use, written in Kotlin.
This is in active development and everything is likely to change. 

## Features
* Completely batched 2D rendering based on OpenGL 2.0
* Different render layers (called "View")
* Support for font rendering via .ttf files, automatic bitmap generation
* Flexible text layout with different alignments
* Audio engine based on OpenAL (just .wav for now, but designed to be easily extendable)
* ECS based scenes, designed to be as lightweight and fast as possible while being fast to develop with
* Serialization format for the ECS which is easy to write/read with additional libraries (like GSON, might use Kotlin serialization in the future though) 
* Automatic asset loading of known (loadable) asset files
* Automatic texture packing during load time
* Completely flexible input system (not only state queries)

In general, everything is designed for fast development while maintaining good performance. 
It is also designed to be very flexible.

## Goal
The main goal of this project is to learn. In the future I might add a visual editor, which is acknowledged in the current design.


