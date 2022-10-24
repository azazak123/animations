package main

import (
	"image/color"
	"math/rand"

	"github.com/faiface/pixel"
	"github.com/faiface/pixel/imdraw"
	"github.com/faiface/pixel/pixelgl"
)

var BACKGROUND color.Color = pixel.RGB(0.52, 0.80, 0.92)
var SUN color.Color = pixel.RGB(0.92, 0.84, 0.1)
var HOUSE color.Color = pixel.RGB(0.7, 0.5, 0.5)
var ROOF color.Color = pixel.RGB(0.7, 0.5, 0.6)

// draw all static contents
func drawStatic(win *pixelgl.Window) {
	win.Clear(BACKGROUND)

	static := imdraw.New(nil)

	// draw ground
	static.Color = pixel.RGB(0.49, 0.78, 0.31)
	static.Push(pixel.V(0, 0), pixel.V(WIDTH, HEIGHT/3))
	static.Rectangle(0)

	// draw flowers
	for i := 0; i < WIDTH; i += 10 {
		for j := 0; j < HEIGHT/3; j += 10 {
			if rand.Float32() < 0.2 {
				static.Color = pixel.RGB(rand.Float64(), rand.Float64(), rand.Float64())
				static.Push(pixel.V(float64(i), float64(j)))
				static.Circle(3, 0)
			}
		}
	}

	// draw house
	//
	// main block
	static.Color = HOUSE
	static.Push(pixel.V(WIDTH/2-100, HEIGHT/4-100), pixel.V(WIDTH/2+100, HEIGHT/4+100))
	static.Rectangle(0)

	// roof
	static.Color = ROOF
	static.Push(pixel.V(WIDTH/2-100, HEIGHT/4+100), pixel.V(WIDTH/2+100, HEIGHT/4+100), pixel.V(WIDTH/2, HEIGHT/4+200))
	static.Polygon(0)

	// chimney
	static.Push(pixel.V(WIDTH/2-50, HEIGHT/4+100), pixel.V(WIDTH/2-25, HEIGHT/4+200))
	static.Rectangle(0)

	// window
	static.Color = pixel.RGB(0.95, 0.93, 0.88)
	static.Push(pixel.V(WIDTH/2-50, HEIGHT/4-50))
	static.Color = BACKGROUND
	static.Push(pixel.V(WIDTH/2+50, HEIGHT/4+50))
	static.Rectangle(0)

	// draw all static elements
	static.Draw(win)
	win.Update()

}
