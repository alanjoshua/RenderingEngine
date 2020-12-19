package engine.particle;

import engine.Math.Vector;

public class FlowParticleGenerator extends ParticleGenerator {

    public int maxParticles;
    public boolean active;
    public long lastCreationTime;  //Nanoseconds
    public Vector posRange = new Vector(0,0,0);
    public float creationPeriodSeconds;
    public Vector velRange = new Vector(0,0,0);
    public Vector accelRange = new Vector(0,0,0);
    public Vector scaleRange = new Vector(0,0,0);
    public float animUpdateRange = 0;

    public FlowParticleGenerator(Particle baseParticle, int maxParticles, float creationPeriodSeconds, String id) {
        super(id);
        this.baseParticle = baseParticle;
        this.maxParticles = maxParticles;
        this.creationPeriodSeconds = creationPeriodSeconds;
    }

    @Override
    public void cleanup() {
        for(Particle p: particles) {
            p.cleanUp();
        }
    }

    @Override
    public void tick(ParticleGeneratorTickInput params) {
        float timeDelta = params.timeDelta;  //Seconds

        long now = System.nanoTime();
        if(lastCreationTime == 0){
            lastCreationTime = now;
        }

        var it = particles.iterator();
        while(it.hasNext()) {
            var particle = it.next();
            if(particle.updateTimeToLive(timeDelta) < 0) {
                it.remove();
            }
            else {
                particle.tick(timeDelta);
            }
        }

        if((now - lastCreationTime)/1000000000.0 >= this.creationPeriodSeconds && particles.size() < maxParticles) {
            createParticle();
            this.lastCreationTime = now;
        }
    }

    public void createParticle() {
        var particle = new Particle(this.baseParticle);
        float sign = Math.random() > 0.5d ? -1.0f : 1.0f;

        Vector speedInc = Vector.randomVector(3).mul(velRange);
        Vector posInc = Vector.randomVector(3).mul(posRange);
        Vector scaleInc = Vector.randomVector(3).mul(scaleRange);
        Vector accelInc = Vector.randomVector(3).mul(accelRange);
        float updateAnimInc = (long)sign *(long)(Math.random() * (float)this.animUpdateRange);

        particle.acceleration = particle.acceleration.add(accelInc);
        particle.pos = particle.pos.add(posInc);
        particle.velocity = particle.velocity.add(speedInc);
        particle.scale = particle.scale.add(scaleInc);
        particle.updateTexture += updateAnimInc;
        particles.add(particle);
    }

}
