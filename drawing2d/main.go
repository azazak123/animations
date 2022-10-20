package main

import (
	"github.com/faiface/pixel"
	"github.com/faiface/pixel/pixelgl"
)

const WIDTH = 1024
const HEIGHT = 768



func run() {
	cfg := pixelgl.WindowConfig{
		Title:  "drawing2d",
		Bounds: pixel.R(0, 0, WIDTH, HEIGHT),
		VSync:  true,
	}
	win, err := pixelgl.NewWindow(cfg)
	if err != nil {
		panic(err)
	}

	// draw static
	drawStatic(win)

	// draw dynamic
	drawDynamic := drawDynamicSetup()

	for !win.Closed() {
		drawDynamic(win)
		win.Update()
	}
}

func main() {
	pixelgl.Run(run)
}

