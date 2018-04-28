package com.example.user.finalhcproject;


import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightState;
import com.philips.lighting.hue.sdk.wrapper.utilities.HueColor;

public class hueColorConversion {

    // XY to RGB conversion provided by Phillips Hue SDK Guide
    public HueColor.RGB convertXYtoRGB(double xVal, double yVal, HueColor.RGB current, double bright) {
        HueColor.RGB rgbValues = current;
        float x = (float)xVal;
        float y = (float)yVal;
        float z = 1.0f - x - y;
        float Y = (float)bright;
        float X = (Y / y) * x;
        float Z = (Y / y) * z;

        float r = (X * 1.656492f) - (Y * 0.354851f) - (Z * 0.255038f);
        float g = (-X * 0.707196f) + (Y * 1.655397f) + (Z * 0.036152f);
        float b = (X * 0.051713f) - (Y * 0.121364f) + (Z * 1.011530f);

        if (r > b && r > g && r > 1.0f) {
            // red is too big
            g = g / r;
            b = b / r;
            r = 1.0f;
        }
        else if (g > b && g > r && g > 1.0f) {
            // green is too big
            r = r / g;
            b = b / g;
            g = 1.0f;
        }
        else if (b > r && b > g && b > 1.0f) {
            // blue is too big
            r = r / b;
            g = g / b;
            b = 1.0f;
        }


        if(r <= 0.0031308f)
            r = 12.92f * r;
        else
            r = (1.0f + 0.055f) * (float)Math.pow(r, (1.0f / 2.4f)) - 0.055f;

        if(g <= 0.0031308f)
            g = 12.92f * g;
        else
            g = (1.0f + 0.055f) * (float)Math.pow(g, (1.0f / 2.4f)) - 0.055f;

        if(b <= 0.0031308f)
            b = 12.92f * b;
        else
            b = (1.0f + 0.055f) * (float)Math.pow(b, (1.0f / 2.4f)) - 0.055f;

        if (r > b && r > g) {
            // red is biggest
            if (r > 1.0f) {
                g = g / r;
                b = b / r;
                r = 1.0f;
            }
        }
        else if (g > b && g > r) {
            // green is biggest
            if (g > 1.0f) {
                r = r / g;
                b = b / g;
                g = 1.0f;
            }
        }
        else if (b > r && b > g) {
            // blue is biggest
            if (b > 1.0f) {
                r = r / b;
                g = g / b;
                b = 1.0f;
            }
        }

        // Values are between 0.0 and 1.0, convert to an int between 0-255
        rgbValues.r = (int)(r * 255);
        rgbValues.g = (int)(g * 255);
        rgbValues.b = (int)(b * 255);
        return  rgbValues;
    }

    public HueColor.RGB getRGBFromLight(LightPoint light) {
        LightState lightState = light.getLightState();
        HueColor hueColor = lightState.getColor();
        HueColor.RGB rgb = hueColor.getRGB();
        HueColor.XY xy = hueColor.getXY();
        rgb = convertXYtoRGB(xy.x, xy.y, rgb, hueColor.getBrightness());
        return rgb;
    }
}
