package com.sam.demorenderscriptcustom

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.renderscript.Allocation
import androidx.renderscript.RenderScript
import com.sam.demorenderscriptcustom.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var mBitmapIn: Bitmap
    private lateinit var mBitmapOutScriptC: Bitmap
    private lateinit var mBitmapOutKotlin: Bitmap

    private lateinit var mRS: RenderScript
    private lateinit var mInAllocation: Allocation
    private lateinit var mOutAllocation: Allocation

    private lateinit var mScriptMono: ScriptC_mono

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mBitmapIn = loadBitmap(R.drawable.bg_iv_hq)
        mBitmapOutScriptC =
            Bitmap.createBitmap(mBitmapIn.width, mBitmapIn.height, Bitmap.Config.ARGB_8888)

        mBitmapOutKotlin =
            Bitmap.createBitmap(mBitmapIn.width, mBitmapIn.height, Bitmap.Config.ARGB_8888)

        buildScipt()
        byKotlinNative()
    }

    private fun buildScipt() {
        val startTimeScripC = System.currentTimeMillis()
        mRS = RenderScript.create(this)
        mInAllocation = Allocation.createFromBitmap(mRS, mBitmapIn)
        mOutAllocation = Allocation.createFromBitmap(mRS, mBitmapOutScriptC)

        mScriptMono = ScriptC_mono(mRS)
//
        mScriptMono.forEach_root(mInAllocation, mOutAllocation);
        mOutAllocation.copyTo(mBitmapOutScriptC);
        binding.ivScriptc.setImageBitmap(mBitmapOutScriptC)
        binding.tvScripC.setText("Time : ${System.currentTimeMillis() - startTimeScripC}ms")
    }

    private fun loadBitmap(resource: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        return BitmapFactory.decodeResource(resources, resource, options)
    }

    private fun byKotlinNative() {
        val startTimeKotlin = System.currentTimeMillis()
        // mBitmapIn is the input bitmap, mBitmapOut is the output bitmap
        var w = mBitmapIn.getWidth();
        var h = mBitmapIn.getHeight();
        var size = w * h;
        var pixels = IntArray(size)
        mBitmapIn.getPixels(pixels, 0, w, 0, 0, w, h);
        for (i in 0..size - 1) {
            var c = pixels[i]; // 0xAARRGGBB
            // we extract red, green and blue components (each one in [0, 255] range)
            var r = c shr 16 and 0xFF
            var g = c shr 8 and 0xFF
            var b = c and 0xFF
            // approximation of the formula using integer arithmetic
            r *= 76;
            g *= 151;
            b *= 29;
            var y = r + g + b shr 8 // luminance
            pixels[i] = y or (y shl 8) or (y shl 16) or (c and -0x1000000)
        }
        mBitmapOutKotlin.setPixels(pixels, 0, w, 0, 0, w, h); // we set the output bitmap’s pixels
        binding.ivKotlinNative.setImageBitmap(mBitmapOutKotlin)
        binding.tvKotlinNative.setText("Time : ${System.currentTimeMillis() - startTimeKotlin}ms")
    }

}