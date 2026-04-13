import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { plansAPI, workoutsAPI, statsAPI } from '../services/api'
import { useWorkoutStore } from '../store/stores'
import toast from 'react-hot-toast'

const MUSCLE_COLORS = {
  'Quadricipiti': '#6c63ff', 'Femorali': '#8b5cf6', 'Petto': '#ef4444',
  'Dorsali': '#f59e0b', 'Spalle': '#22c55e', 'Bicipiti': '#06b6d4',
  'Tricipiti': '#0ea5e9', 'Core': '#ec4899', 'Polpacci': '#84cc16', 'Glutei': '#a78bfa'
}

function fmtTime(s) {
  const m = Math.floor(s / 60)
  return `${m}:${String(s % 60).padStart(2, '0')}`
}

export default function LogPage() {
  const [plans, setPlans] = useState([])
  const [selectedPlan, setSelectedPlan] = useState(null)
  const [selectedDay, setSelectedDay] = useState(null)
  const [showFinish, setShowFinish] = useState(false)
  const [exInfoIdx, setExInfoIdx] = useState(null)

  const { activeSession, sessionTimer, restTimer, startSession, updateSession, stopSession, startRestTimer, stopRestTimer } = useWorkoutStore()
  const navigate = useNavigate()

  useEffect(() => {
    if (!activeSession) {
      plansAPI.getAll().then(r => {
        setPlans(r.data.data)
        const active = r.data.data.find(p => p.isActive)
        if (active) setSelectedPlan(active)
      }).catch(() => {})
    }
  }, [activeSession])

  const begin = async () => {
    if (!selectedPlan || !selectedDay) { toast.error('Seleziona scheda e giorno'); return }
    try {
      const { data } = await workoutsAPI.start({
        planId: selectedPlan.id,
        workoutDayId: selectedDay.id,
        dayName: selectedDay.name,
        startedAt: new Date().toISOString()
      })
      const session = data.data
      // Enrich with local exercise data for UI
      const enriched = {
        ...session,
        exercises: (selectedDay.exercises || []).map(ex => ({
          exerciseId: ex.exerciseId,
          exerciseName: ex.exerciseName,
          muscleGroup: ex.muscleGroup,
          restSeconds: ex.restSeconds || 90,
          repsRange: ex.repsRange,
          planNotes: ex.notes,
          exerciseNotes: ex.exerciseNotes,
          sets: Array.from({ length: ex.sets || 3 }, (_, i) => ({
            setNumber: i + 1,
            weightKg: ex.targetWeightKg || 0,
            reps: parseInt(ex.repsRange) || 10,
            rir: 2,
            isCompleted: false
          }))
        }))
      }
      startSession(enriched)
      toast.success(`${selectedDay.name} — Iniziato! 💪`)
    } catch { toast.error('Errore avvio sessione') }
  }

  const toggleSet = (exIdx, setIdx) => {
    updateSession(s => {
      const exercises = s.exercises.map((ex, ei) => {
        if (ei !== exIdx) return ex
        const sets = ex.sets.map((set, si) => {
          if (si !== setIdx) return set
          const done = !set.isCompleted
          if (done) startRestTimer(ex.restSeconds || 90)
          return { ...set, isCompleted: done }
        })
        return { ...ex, sets }
      })
      return { ...s, exercises }
    })
  }

  const updateSetVal = (exIdx, setIdx, field, val) => {
    updateSession(s => ({
      ...s,
      exercises: s.exercises.map((ex, ei) =>
        ei !== exIdx ? ex : {
          ...ex,
          sets: ex.sets.map((set, si) =>
            si !== setIdx ? set : { ...set, [field]: parseFloat(val) || 0 }
          )
        }
      )
    }))
  }

  const addSet = (exIdx) => {
    updateSession(s => ({
      ...s,
      exercises: s.exercises.map((ex, ei) => {
        if (ei !== exIdx) return ex
        const last = ex.sets.at(-1) || { weightKg: 0, reps: 10, rir: 2, isCompleted: false }
        return {
          ...ex,
          sets: [...ex.sets, { ...last, setNumber: ex.sets.length + 1, isCompleted: false }]
        }
      })
    }))
  }

  const getSuggest = async (exIdx) => {
    const ex = activeSession.exercises[exIdx]
    try {
      const { data } = await statsAPI.getSuggestedLoad(ex.exerciseId)
      const s = data.data
      toast(`🤖 ${s.exerciseName}\n${s.reason}\n1RM stimato: ${s.estimated1rm}kg`, { duration: 5000 })
    } catch { toast.error('Nessun dato precedente') }
  }

  const finishSession = async () => {
    try {
      const payload = {
        sessionId: activeSession.id,
        finishedAt: new Date().toISOString(),
        exercises: activeSession.exercises.map((ex, ei) => ({
          exerciseId: ex.exerciseId,
          exerciseName: ex.exerciseName,
          muscleGroup: ex.muscleGroup,
          exerciseOrder: ei,
          sets: ex.sets.map((s, si) => ({
            setNumber: si + 1,
            weightKg: s.weightKg,
            reps: s.reps,
            rir: s.rir,
            isCompleted: s.isCompleted
          }))
        }))
      }
      await workoutsAPI.finish(payload)
      stopSession()
      toast.success('Sessione salvata! 🎉')
      setShowFinish(false)
      navigate('/')
    } catch { toast.error('Errore nel salvataggio') }
  }

  // ─── PRE-SESSION: PICK PLAN/DAY ──────────────────────────────────────────

  if (!activeSession) {
    return (
      <div>
        <div className="page-header">
          <span className="page-title">Nuovo Workout</span>
        </div>

        {plans.length === 0 ? (
          <div className="empty">
            <div className="empty-icon">📋</div>
            <div className="empty-text">Nessuna scheda trovata.<br />Crea prima una scheda.</div>
          </div>
        ) : (
          <>
            <div className="section-label" style={{ marginTop: 12 }}>Seleziona Scheda</div>
            {plans.map(p => (
              <div
                key={p.id}
                className={`workout-item${selectedPlan?.id === p.id ? ' active-plan' : ''}`}
                onClick={() => { setSelectedPlan(p); setSelectedDay(null) }}
              >
                <div className="workout-emoji">{p.isActive ? '⭐' : '📋'}</div>
                <div style={{ flex: 1 }}>
                  <div className="workout-name">{p.name}</div>
                  <div className="workout-meta">{p.split} · {p.goal}</div>
                </div>
                {p.isActive && <span className="badge badge-green">Attiva</span>}
              </div>
            ))}

            {selectedPlan && (
              <>
                <div className="section-label" style={{ marginTop: 8 }}>Seleziona Giorno</div>
                <div style={{ display: 'flex', gap: 8, padding: '0 16px', flexWrap: 'wrap' }}>
                  {(selectedPlan.workoutDays || []).map(day => (
                    <button
                      key={day.id}
                      className={`btn ${selectedDay?.id === day.id ? '' : 'btn-ghost'}`}
                      onClick={() => setSelectedDay(day)}
                    >
                      {day.name}
                    </button>
                  ))}
                </div>
              </>
            )}

            {selectedPlan && selectedDay && (
              <div style={{ padding: '20px 16px 0' }}>
                <button className="btn btn-block btn-success" onClick={begin}>
                  ▶ Inizia {selectedDay.name}
                </button>
              </div>
            )}
          </>
        )}
      </div>
    )
  }

  // ─── ACTIVE SESSION ───────────────────────────────────────────────────────

  const totalDone = activeSession.exercises.reduce(
    (a, ex) => a + ex.sets.filter(s => s.isCompleted).length, 0)
  const totalSets = activeSession.exercises.reduce((a, ex) => a + ex.sets.length, 0)

  return (
    <div>
      {/* Session header */}
      <div className="session-header">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
          <div>
            <div className="session-title">{activeSession.dayName}</div>
            <div className="session-time">{fmtTime(sessionTimer)} · {totalDone}/{totalSets} serie</div>
          </div>
          <button
            className="btn btn-sm"
            style={{ background: 'rgba(255,255,255,0.2)', color: '#fff' }}
            onClick={() => setShowFinish(true)}
          >
            Finisci
          </button>
        </div>
      </div>

      {/* Rest timer */}
      {restTimer && (
        <div className="timer-widget">
          <span>⏱</span>
          <div className="timer-display">{fmtTime(restTimer.left)}</div>
          <div className="timer-bar">
            <div className="timer-bar-fill"
              style={{ width: `${(restTimer.left / restTimer.total) * 100}%` }} />
          </div>
          <button className="btn btn-ghost btn-sm" onClick={stopRestTimer}>✕</button>
        </div>
      )}

      {/* Exercises */}
      {activeSession.exercises.map((ex, ei) => (
        <div key={ei} className="ex-logger">
          <div className="ex-logger-header">
            <div style={{ flex: 1, minWidth: 0 }}>
              <div className="ex-logger-name">{ex.exerciseName}</div>
              <div style={{ fontSize: 12, color: 'var(--text2)', marginTop: 2, display: 'flex', alignItems: 'center', gap: 6 }}>
                <span style={{
                  display: 'inline-block', width: 8, height: 8, borderRadius: '50%',
                  background: MUSCLE_COLORS[ex.muscleGroup] || '#888'
                }} />
                {ex.muscleGroup}
                {ex.repsRange && <span style={{ color: 'var(--text3)' }}>· {ex.repsRange} reps</span>}
              </div>
              {ex.planNotes && (
                <div style={{ fontSize: 11, color: 'var(--accent2)', marginTop: 3 }}>{ex.planNotes}</div>
              )}
            </div>
            <div style={{ display: 'flex', gap: 6 }}>
              {(ex.exerciseNotes) && (
                <button className="btn btn-ghost btn-sm" onClick={() => setExInfoIdx(exInfoIdx === ei ? null : ei)} title="Note tecniche">ℹ</button>
              )}
              <button className="btn btn-ghost btn-sm" onClick={() => getSuggest(ei)}>🤖</button>
            </div>
          </div>
          {exInfoIdx === ei && ex.exerciseNotes && (
            <div style={{ margin: '0 12px 10px', padding: '10px 12px', background: 'var(--bg3)', borderRadius: 8, fontSize: 13, color: 'var(--text2)', lineHeight: 1.6 }}>
              {ex.exerciseNotes}
            </div>
          )}

          <table className="sets-table">
            <thead>
              <tr>
                <th>Set</th><th>kg</th><th>Reps</th><th>RIR</th><th>✓</th>
              </tr>
            </thead>
            <tbody>
              {ex.sets.map((set, si) => (
                <tr key={si}>
                  <td style={{ fontSize: 12, color: 'var(--text3)' }}>{si + 1}</td>
                  <td>
                    <input className="set-input" type="number" step="2.5" min="0"
                      value={set.weightKg}
                      onChange={e => updateSetVal(ei, si, 'weightKg', e.target.value)} />
                  </td>
                  <td>
                    <input className="set-input" type="number" min="1"
                      value={set.reps}
                      onChange={e => updateSetVal(ei, si, 'reps', e.target.value)} />
                  </td>
                  <td>
                    <input className="set-input" type="number" min="0" max="5"
                      value={set.rir}
                      onChange={e => updateSetVal(ei, si, 'rir', e.target.value)}
                      style={{ width: 44 }} />
                  </td>
                  <td>
                    <button
                      className={`set-done-btn${set.isCompleted ? ' done' : ''}`}
                      onClick={() => toggleSet(ei, si)}
                    >
                      {set.isCompleted ? '✓' : '○'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div style={{ padding: '8px 12px' }}>
            <button className="btn btn-ghost btn-sm" onClick={() => addSet(ei)}>+ Set</button>
          </div>
        </div>
      ))}

      <div style={{ padding: '8px 16px 16px' }}>
        <button className="btn btn-block" onClick={() => setShowFinish(true)}>
          Finisci Sessione
        </button>
      </div>

      {/* Finish modal */}
      {showFinish && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-handle" />
            <div className="modal-title">🏆 Sessione Completata!</div>

            <div className="stats-strip" style={{ padding: '0', marginBottom: 16 }}>
              <div className="stat-card">
                <div className="stat-num">{totalDone}</div>
                <div className="stat-label">Serie fatte</div>
              </div>
              <div className="stat-card">
                <div className="stat-num">{fmtTime(sessionTimer)}</div>
                <div className="stat-label">Durata</div>
              </div>
              <div className="stat-card">
                <div className="stat-num">
                  {(activeSession.exercises.reduce((a, ex) =>
                    a + ex.sets.filter(s => s.isCompleted).reduce((b, s) => b + s.weightKg * s.reps, 0), 0) / 1000
                  ).toFixed(1)}k
                </div>
                <div className="stat-label">kg Totali</div>
              </div>
            </div>

            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn btn-success" style={{ flex: 1 }} onClick={finishSession}>
                Salva & Chiudi
              </button>
              <button className="btn btn-ghost" onClick={() => setShowFinish(false)}>
                Continua
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
