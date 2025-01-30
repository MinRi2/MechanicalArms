package mechanicalArms.math;

import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;

/**
 * @author minri2
 * Create by 2025/1/30
 */
public class ArmsInverseKinematics{
    private static final Vec2 v1 = new Vec2(), v2 = new Vec2();

    public static void solveFabrik(Seq<Vec2> joints, Vec2 base, Vec2 target, float tolerance, int maxIteration){
        float[] jointLengths = new float[joints.size];

        Vec2 last = base;
        for(int i = 0; i < joints.size; i++){
            Vec2 v = joints.get(i);
            jointLengths[i] = last.dst(v);
            last = v;
        }

        float totalLength = 0;
        for(float len : jointLengths){
            totalLength += len;
        }

        if(base.dst(target) >= totalLength){
            Vec2 direction = v1.set(target).sub(base).nor();
            Vec2 lastJointPos = v2.set(base);
            for(int i = 0; i < joints.size; i++) {
                lastJointPos.add(direction.scl(jointLengths[i]));
                joints.get(i).set(lastJointPos);
            }
            return;
        }

        Vec2 effector = joints.peek();

//        int count = 0;
        for(int i = 0; i < maxIteration; i++){
//            count = i + 1;

            // backward
            effector.set(target);
            for(int j = joints.size - 2; j >= 0; j--) {
                Vec2 current = joints.get(j);
                Vec2 next = joints.get(j + 1);

                Vec2 dir = v1.set(current).sub(next).setLength(jointLengths[j + 1]);
                current.set(next).add(dir);
            }

            // forward
            Vec2 prev = base;
            for(int j = 0; j < joints.size; j++) {
                Vec2 current = joints.get(j);

                Vec2 dir = v1.set(current).sub(prev).setLength(jointLengths[j]);
                current.set(prev).add(dir);

                prev = current;
            }

            float dst = effector.dst(target);
            if(dst < tolerance){
                break;
            }
        }

//        Log.info("Count: @", count);
    }

}
