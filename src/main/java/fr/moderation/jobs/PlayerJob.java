package fr.moderation.jobs;

import java.util.UUID;

public class PlayerJob {
    
    private final UUID playerId;
    private final JobType jobType;
    private int level;
    private double currentXP;
    private double totalXP;
    private final long createdAt;
    
    public PlayerJob(UUID playerId, JobType jobType) {
        this.playerId = playerId;
        this.jobType = jobType;
        this.level = 1;
        this.currentXP = 0;
        this.totalXP = 0;
        this.createdAt = System.currentTimeMillis();
    }
    
    public PlayerJob(UUID playerId, JobType jobType, int level, double currentXP, double totalXP, long createdAt) {
        this.playerId = playerId;
        this.jobType = jobType;
        this.level = level;
        this.currentXP = currentXP;
        this.totalXP = totalXP;
        this.createdAt = createdAt;
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public JobType getJobType() {
        return jobType;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public double getCurrentXP() {
        return currentXP;
    }
    
    public void setCurrentXP(double currentXP) {
        this.currentXP = currentXP;
    }
    
    public double getTotalXP() {
        return totalXP;
    }
    
    public void setTotalXP(double totalXP) {
        this.totalXP = totalXP;
    }
    
    public void addXP(double amount) {
        this.currentXP += amount;
        this.totalXP += amount;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Calcule l'XP requis pour le prochain niveau
     * Niveau 1→2 = 100 XP
     * Formule: 100 * N pour niveau N→N+1
     */
    public double getXPForNextLevel() {
        return 100.0 * level;
    }
    
    /**
     * Calcule le pourcentage de progression vers le prochain niveau
     */
    public double getProgressPercent() {
        double required = getXPForNextLevel();
        if (required == 0) return 100.0;
        return Math.min(100.0, (currentXP / required) * 100.0);
    }
}
