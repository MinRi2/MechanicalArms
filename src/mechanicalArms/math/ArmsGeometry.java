package mechanicalArms.math;

import arc.math.*;
import arc.math.geom.*;

/**
 * @author minri2
 * Create by 2025/1/25
 */
public class ArmsGeometry{
    private static final Vec2 v1 = new Vec2(), v2 = new Vec2(), v3 = new Vec2();

    public static int getCircleInspectedPoints(float x1, float y1, float r1, float x2, float y2, float r2, Vec2 out1, Vec2 out2){
        if(Mathf.equal(x1, x2) && Mathf.equal(y1, y2)){
            x1 -= 0.001f;
            y1 -= 0.001f;
        }

        float dst2 = Mathf.dst2(x1, y1, x2, y2);
        float circleDst2 = (r1 + r2) * (r1 + r2);
        if(dst2 > circleDst2){
            return 0;
        }

        float dst = Mathf.sqrt(dst2);

        // cos可能超出范围 精度???
        float cos = (r1 * r1 - r2 * r2 + dst2) / (2 * r1 * dst);

        cos = Mathf.clamp(cos, -1, 1);

        float sin = Mathf.sqrt(1 - cos * cos);

        float scala = r1 * cos / dst;
        float norscala = r1 * sin / dst;

        Vec2 dv = v1.set(x2, y2).sub(x1, y1);
        Vec2 proPoint = v2.set(dv).scl(scala);
        Vec2 norv = v3.set(dv.y, -dv.x).scl(norscala);

        if(Mathf.equal(dst, Math.abs(r1 - r2)) || Mathf.equal(dst, r1 + r2)){
            out1.set(proPoint).add(norv).add(x1, y1);
            out2.set(out1);
            return 1;
        }

        out1.set(proPoint).add(norv).add(x1, y1);
        out2.set(proPoint).add(norv.scl(-1)).add(x1, y1);
        return 2;
    }
}
