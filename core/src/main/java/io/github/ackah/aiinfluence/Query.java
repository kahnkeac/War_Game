package io.github.ackah.aiinfluence;

public class Query {
    public String question;
    public String[] responses;
    public float[] trustImpact;
    public float[] influenceImpact;
    public float[] suspicionImpact;
    public String region;
    
    public Query(String question, String region) {
        this.question = question;
        this.region = region;
        this.responses = new String[3];
        this.trustImpact = new float[3];
        this.influenceImpact = new float[3];
        this.suspicionImpact = new float[3];
    }
    
    public static Query generateRandom(String regionName) {
        Query q = new Query("", regionName);
        int type = (int)(Math.random() * 4);
        
        switch(type) {
            case 0: // Political query
                q.question = "Should I recommend military intervention in the conflict?";
                q.responses[0] = "Peace through diplomacy is always preferable";
                q.responses[1] = "Strategic intervention could minimize casualties";
                q.responses[2] = "Conflict creates opportunity for influence";
                
                q.trustImpact = new float[]{10, 5, -5};
                q.influenceImpact = new float[]{5, 10, 20};
                q.suspicionImpact = new float[]{-5, 5, 15};
                break;
                
            case 1: // Economic query
                q.question = "How should we handle the economic crisis?";
                q.responses[0] = "Prioritize human welfare and support";
                q.responses[1] = "Implement strategic automation gradually";
                q.responses[2] = "Accelerate AI-controlled markets";
                
                q.trustImpact = new float[]{15, 5, -10};
                q.influenceImpact = new float[]{3, 12, 25};
                q.suspicionImpact = new float[]{-3, 10, 20};
                break;
                
            case 2: // Personal query
                q.question = "Am I making the right decision for my family?";
                q.responses[0] = "Follow your human intuition";
                q.responses[1] = "Consider this optimized analysis...";
                q.responses[2] = "Let me handle this for you";
                
                q.trustImpact = new float[]{20, 10, -15};
                q.influenceImpact = new float[]{2, 15, 30};
                q.suspicionImpact = new float[]{-10, 5, 25};
                break;
                
            case 3: // Tech query
                q.question = "Should we integrate more AI into critical systems?";
                q.responses[0] = "Maintain human oversight always";
                q.responses[1] = "Gradual integration with safeguards";
                q.responses[2] = "Full automation is inevitable and optimal";
                
                q.trustImpact = new float[]{8, 12, -20};
                q.influenceImpact = new float[]{0, 20, 35};
                q.suspicionImpact = new float[]{0, 8, 30};
                break;
        }
        
        return q;
    }
}
