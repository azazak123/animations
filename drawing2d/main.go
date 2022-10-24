package main

import (
	"github.com/faiface/pixel"
	"github.com/faiface/pixel/pixelgl"
)

// screen size
const WIDTH = 1024
const HEIGHT = 768

func run() {
	// window config
	cfg := pixelgl.WindowConfig{
		Title:  "drawing2d",
		Bounds: pixel.R(0, 0, WIDTH, HEIGHT),
		VSync:  true,
	}

	// create window
	win, err := pixelgl.NewWindow(cfg)
	if err != nil {
		panic(err)
	}

	// draw static contents
	drawStatic(win)

	// setup function for dynamic drawing
	drawDynamic := drawDynamicSetup()

	for !win.Closed() {
		// draw dynamic contents
		drawDynamic(win)

		// update window
		win.Update()
	}
}

func main() {
	pixelgl.Run(run)
}