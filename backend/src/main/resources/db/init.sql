-- FitDiary Database Schema
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INTEGER,
    weight_kg DECIMAL(5,2),
    height_cm DECIMAL(5,2),
    goal VARCHAR(50),
    level VARCHAR(50) DEFAULT 'BEGINNER',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Refresh tokens
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(512) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Workout plans
CREATE TABLE workout_plans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    split VARCHAR(100),
    days_per_week INTEGER DEFAULT 3,
    goal VARCHAR(100),
    is_active BOOLEAN DEFAULT FALSE,
    is_template BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Workout days within a plan
CREATE TABLE workout_days (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plan_id UUID NOT NULL REFERENCES workout_plans(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    day_order INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Exercises (global library)
CREATE TABLE exercises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    muscle_group VARCHAR(100),
    category VARCHAR(100),
    notes TEXT,
    is_custom BOOLEAN DEFAULT FALSE,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Planned exercises in a workout day
CREATE TABLE planned_exercises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workout_day_id UUID NOT NULL REFERENCES workout_days(id) ON DELETE CASCADE,
    exercise_id UUID NOT NULL REFERENCES exercises(id),
    exercise_order INTEGER NOT NULL,
    sets INTEGER DEFAULT 3,
    reps_range VARCHAR(20) DEFAULT '8-12',
    rest_seconds INTEGER DEFAULT 90,
    target_weight_kg DECIMAL(6,2) DEFAULT 0,
    notes TEXT
);

-- Workout sessions (logged workouts)
CREATE TABLE workout_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id UUID REFERENCES workout_plans(id),
    workout_day_id UUID REFERENCES workout_days(id),
    day_name VARCHAR(255),
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    duration_seconds INTEGER,
    total_volume_kg DECIMAL(10,2) DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Exercise logs within a session
CREATE TABLE exercise_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES workout_sessions(id) ON DELETE CASCADE,
    exercise_id UUID NOT NULL REFERENCES exercises(id),
    exercise_name VARCHAR(255),
    muscle_group VARCHAR(100),
    exercise_order INTEGER NOT NULL
);

-- Set logs within an exercise log
CREATE TABLE set_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    exercise_log_id UUID NOT NULL REFERENCES exercise_logs(id) ON DELETE CASCADE,
    set_number INTEGER NOT NULL,
    weight_kg DECIMAL(6,2) NOT NULL DEFAULT 0,
    reps INTEGER NOT NULL DEFAULT 0,
    rir INTEGER DEFAULT 2,
    duration_seconds INTEGER,
    is_completed BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Personal records
CREATE TABLE personal_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    exercise_id UUID NOT NULL REFERENCES exercises(id),
    weight_kg DECIMAL(6,2) NOT NULL,
    reps INTEGER NOT NULL,
    estimated_1rm DECIMAL(6,2),
    achieved_at TIMESTAMP NOT NULL,
    session_id UUID REFERENCES workout_sessions(id),
    UNIQUE(user_id, exercise_id)
);

-- Indexes
CREATE INDEX idx_sessions_user ON workout_sessions(user_id);
CREATE INDEX idx_sessions_date ON workout_sessions(started_at);
CREATE INDEX idx_set_logs_exercise ON set_logs(exercise_log_id);
CREATE INDEX idx_plans_user ON workout_plans(user_id);
CREATE INDEX idx_pr_user_exercise ON personal_records(user_id, exercise_id);

-- Seed exercise library
INSERT INTO exercises (id, name, muscle_group, category) VALUES
    (uuid_generate_v4(), 'Squat con Bilanciere', 'Quadricipiti', 'Compound'),
    (uuid_generate_v4(), 'Panca Piana', 'Petto', 'Compound'),
    (uuid_generate_v4(), 'Stacco da Terra', 'Femorali', 'Compound'),
    (uuid_generate_v4(), 'Trazioni', 'Dorsali', 'Compound'),
    (uuid_generate_v4(), 'Shoulder Press', 'Spalle', 'Compound'),
    (uuid_generate_v4(), 'Rematore con Bilanciere', 'Dorsali', 'Compound'),
    (uuid_generate_v4(), 'Leg Press', 'Quadricipiti', 'Compound'),
    (uuid_generate_v4(), 'Romanian Deadlift', 'Femorali', 'Compound'),
    (uuid_generate_v4(), 'Panca Inclinata', 'Petto', 'Compound'),
    (uuid_generate_v4(), 'Curl con Bilanciere', 'Bicipiti', 'Isolation'),
    (uuid_generate_v4(), 'Tricipiti Pushdown', 'Tricipiti', 'Isolation'),
    (uuid_generate_v4(), 'Leg Extension', 'Quadricipiti', 'Isolation'),
    (uuid_generate_v4(), 'Leg Curl', 'Femorali', 'Isolation'),
    (uuid_generate_v4(), 'Alzate Laterali', 'Spalle', 'Isolation'),
    (uuid_generate_v4(), 'Dip alle Parallele', 'Tricipiti', 'Compound'),
    (uuid_generate_v4(), 'Pull-up', 'Dorsali', 'Compound'),
    (uuid_generate_v4(), 'Plank', 'Core', 'Core'),
    (uuid_generate_v4(), 'Calf Raise', 'Polpacci', 'Isolation');
