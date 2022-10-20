package main

import (
	"math"

	"github.com/faiface/pixel"
	"github.com/faiface/pixel/imdraw"
	"github.com/faiface/pixel/pixelgl"
)

const SUN_RADIUS = 50
const MAXIMUM_Y = HEIGHT - 50
const COEFFICIENT = 0.001

func drawDynamicSetup() func(win *pixelgl.Window) {
	sunX := -100
	smokeY := HEIGHT/4 + 210
	dynamic := imdraw.New(nil)

	return func(win *pixelgl.Window) {
		cord := pixel.V(float64(sunX), MAXIMUM_Y-COEFFICIENT*math.Pow(math.Abs(float64(sunX-WIDTH/2)), 2))

		if sunX <= WIDTH+100 {
			dynamic.Color = SUN
			dynamic.Push(cord)
			dynamic.Circle(50, 0)
		}

		if smokeY <= HEIGHT/4+310 {
			dynamic.Color = pixel.RGBA{R: 0, G: 0, B: 0, A: float64(HEIGHT/4+310-smokeY) / 100.0}
			dynamic.Push(pixel.V(WIDTH/2-40.0, float64(smokeY)))
			dynamic.Circle(10, 0)
		}

		dynamic.Draw(win)
		win.Update()

		for j := 0; j < 1000000; j++ {
		}

		if sunX <= WIDTH+100 {
			dynamic.Color = BACKGROUND
			dynamic.Push(cord)
			dynamic.Circle(50, 0)

			sunX++

			if sunX == WIDTH+100 {
				sunX = -100
			}
		}

		if smokeY <= HEIGHT/4+310 {
			dynamic.Color = BACKGROUND
			dynamic.Push(pixel.V(WIDTH/2-40.0, float64(smokeY)))
			dynamic.Circle(10, 0)

			smokeY++

			if smokeY == HEIGHT/4+310 {
				smokeY = HEIGHT/4 + 210
			}
		}

	}
}
