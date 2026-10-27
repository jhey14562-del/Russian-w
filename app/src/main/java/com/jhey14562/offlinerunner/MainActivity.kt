package com.jhey14562.offlinerunner

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.max
import kotlin.random.Random

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(GameView(this))
    }
}

private data class Obstacle(var x: Float, val w: Float, val h: Float)

private class GameView(context: Context) : View(context) {
    private val prefs = context.getSharedPreferences("runner_scores", Context.MODE_PRIVATE)
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val obstacles = mutableListOf<Obstacle>()
    private var initialized = false
    private var ground = 0f
    private var size = 0f
    private var y = 0f
    private var vy = 0f
    private var speed = 0f
    private var spawn = 0f
    private var score = 0f
    private var best = prefs.getInt("best", 0)
    private var started = false
    private var over = false
    private var last = 0L
    private var offset = 0f
    private val gravity get() = height * 2.8f
    private val jump get() = -height * 0.9f

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        if (width <= 0 || height <= 0) return
        val now = System.nanoTime()
        val dt = if (last == 0L) 0.016f else ((now-last)/1_000_000_000f).coerceIn(0f, .035f)
        last = now
        if (!initialized) {
            size = width * .085f
            ground = height * .78f
            y = ground-size
            speed = width*.62f
            initialized = true
        }
        c.drawColor(Color.rgb(16,23,42))
        p.color = Color.rgb(28,45,70)
        for (i in 0..5) {
            val x = i*width/4f - (offset*.12f % (width/4f))
            val path = android.graphics.Path()
            path.moveTo(x, height*.64f)
            path.lineTo(x+width*.16f, height*.49f+(i%3)*height*.025f)
            path.lineTo(x+width*.32f, height*.64f)
            path.close()
            c.drawPath(path,p)
        }
        if (started && !over) update(dt)
        p.color = Color.rgb(28,39,57)
        c.drawRect(0f,ground,width.toFloat(),height.toFloat(),p)
        p.color = Color.rgb(0,194,168)
        c.drawRect(0f,ground,width.toFloat(),ground+height*.009f,p)
        p.color = Color.rgb(74,94,119)
        var dx = -(offset % (width*.18f))
        while (dx < width) {
            c.drawRoundRect(dx,ground+height*.09f,dx+width*.09f,ground+height*.102f,8f,8f,p)
            dx += width*.18f
        }
        val px = width*.19f
        p.color = Color.rgb(0,194,168)
        c.drawRoundRect(px,y,px+size,y+size,size*.2f,size*.2f,p)
        p.color = Color.WHITE
        c.drawCircle(px+size*.68f,y+size*.32f,size*.075f,p)
        p.color = Color.rgb(16,23,42)
        c.drawCircle(px+size*.70f,y+size*.32f,size*.035f,p)
        for (o in obstacles) {
            val top = ground-o.h
            p.color = Color.rgb(255,105,97)
            c.drawRoundRect(o.x,top,o.x+o.w,ground,10f,10f,p)
            p.color = Color.rgb(255,174,120)
            c.drawRect(o.x+o.w*.18f,top+o.h*.12f,o.x+o.w*.32f,ground-o.h*.12f,p)
        }
        p.color = Color.WHITE
        p.textSize = width*.05f
        p.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
        c.drawText("SCORE  ${score.toInt()}",width*.06f,height*.075f,p)
        p.color = Color.rgb(167,185,207)
        p.textSize = width*.036f
        c.drawText("BEST  $best",width*.06f,height*.115f,p)
        if (!started) message(c,"OFFLINE RUNNER","Tap to start • Tap to jump")
        else if (over) message(c,"GAME OVER","Score: ${score.toInt()} • Tap to retry")
        postInvalidateOnAnimation()
    }

    private fun update(dt: Float) {
        offset += speed*dt
        score += dt*12f
        speed = minOf(width*(.62f+score/1600f),width*1.05f)
        vy += gravity*dt
        y += vy*dt
        if (y > ground-size) { y=ground-size; vy=0f }
        spawn -= dt
        if (spawn <= 0f) {
            obstacles.add(Obstacle(width+size,size*Random.nextFloat().coerceIn(.55f,.9f),size*Random.nextFloat().coerceIn(.65f,1.35f)))
            spawn = Random.nextFloat().coerceIn(.9f,1.55f)*(width*.65f/speed).coerceIn(.65f,1.25f)
        }
        obstacles.forEach { it.x -= speed*dt }
        obstacles.removeAll { it.x+it.w < 0 }
        val player = RectF(width*.19f+size*.12f,y+size*.12f,width*.19f+size*.88f,y+size*.92f)
        if (obstacles.any { RectF(it.x+it.w*.1f,ground-it.h,it.x+it.w*.9f,ground).intersect(player) }) {
            over = true
            val s = score.toInt()
            if (s > best) { best=s; prefs.edit().putInt("best",best).apply() }
        }
    }

    private fun message(c: Canvas, title: String, subtitle: String) {
        p.color = 0xCC10172A.toInt()
        c.drawRoundRect(width*.07f,height*.34f,width*.93f,height*.57f,28f,28f,p)
        p.textAlign = Paint.Align.CENTER
        p.color = Color.WHITE
        p.typeface = android.graphics.Typeface.create("sans-serif-black",android.graphics.Typeface.BOLD)
        p.textSize = width*.07f
        c.drawText(title,width/2f,height*.425f,p)
        p.color = Color.rgb(0,220,190)
        p.typeface = android.graphics.Typeface.create("sans-serif-medium",android.graphics.Typeface.NORMAL)
        p.textSize = width*.036f
        c.drawText(subtitle,width/2f,height*.49f,p)
        p.textAlign = Paint.Align.LEFT
    }

    private fun tap() {
        if (!started || over) {
            started=true; over=false; score=0f; speed=width*.62f; spawn=.8f
            obstacles.clear(); y=ground-size; vy=0f
        } else if (y >= ground-size-2f) vy=jump
        invalidate()
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) { tap(); return true }
        return true
    }
}
