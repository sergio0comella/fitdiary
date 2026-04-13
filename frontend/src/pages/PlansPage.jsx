import { useEffect, useState } from 'react'
import { plansAPI, exercisesAPI } from '../services/api'
import toast from 'react-hot-toast'

const MUSCLE_COLORS = {
  'Quadricipiti': '#6c63ff', 'Femorali': '#8b5cf6', 'Glutei': '#a78bfa',
  'Petto': '#ef4444', 'Dorsali': '#f59e0b', 'Spalle': '#22c55e',
  'Bicipiti': '#06b6d4', 'Tricipiti': '#0ea5e9', 'Core': '#ec4899', 'Polpacci': '#84cc16'
}

const TEMPLATES = {
  PPL: {
    name: 'PPL Ipertrofia', split: 'Push/Pull/Legs', daysPerWeek: 6, goal: 'Massa',
    workoutDays: [
      { name: 'Push', dayOrder: 0, exercises: [] },
      { name: 'Pull', dayOrder: 1, exercises: [] },
      { name: 'Legs', dayOrder: 2, exercises: [] },
    ]
  },
  '5x5': {
    name: 'StrongLifts 5x5', split: 'Full Body', daysPerWeek: 3, goal: 'Forza',
    workoutDays: [
      { name: 'Giorno A', dayOrder: 0, exercises: [] },
      { name: 'Giorno B', dayOrder: 1, exercises: [] },
    ]
  },
  FullBody: {
    name: 'Full Body 3x', split: 'Full Body', daysPerWeek: 3, goal: 'Salute',
    workoutDays: [{ name: 'Full Body', dayOrder: 0, exercises: [] }]
  }
}

export default function PlansPage() {
  const [plans, setPlans] = useState([])
  const [showNew, setShowNew] = useState(false)
  const [detail, setDetail] = useState(null)
  const [showAddEx, setShowAddEx] = useState(null) // { planId, dayId }
  const [exSearch, setExSearch] = useState('')
  const [exMuscleFilter, setExMuscleFilter] = useState('Tutti')
  const [exCatFilter, setExCatFilter] = useState('Tutti')
  const [exForm, setExForm] = useState({ sets: 3, repsRange: '8-12', restSeconds: 90, targetWeightKg: '', notes: '' })
  const [exercises, setExercises] = useState([])
  const [selectedEx, setSelectedEx] = useState(null)
  const [showCustomEx, setShowCustomEx] = useState(false)
  const [customExForm, setCustomExForm] = useState({ name: '', muscleGroup: 'Petto', category: 'Compound', notes: '' })
  const [form, setForm] = useState({ name: '', split: 'Push/Pull/Legs', daysPerWeek: '3', goal: 'Massa' })
  const [editEx, setEditEx] = useState(null) // { planId, dayId, ex }
  const [editForm, setEditForm] = useState({ sets: 3, repsRange: '8-12', restSeconds: 90, targetWeightKg: '', notes: '' })

  useEffect(() => {
    load()
    exercisesAPI.getAll().then(r => setExercises(r.data.data)).catch(() => {})
  }, [])

  const load = () =>
    plansAPI.getAll().then(r => setPlans(r.data.data)).catch(() => {})

  const createPlan = async (e) => {
    e.preventDefault()
    try {
      const days = generateDays(form.split, parseInt(form.daysPerWeek))
      await plansAPI.create({ ...form, daysPerWeek: parseInt(form.daysPerWeek), workoutDays: days })
      toast.success('Scheda creata!')
      setShowNew(false)
      load()
    } catch { toast.error('Errore nella creazione') }
  }

  const loadTemplate = async (key) => {
    try {
      await plansAPI.create(TEMPLATES[key])
      toast.success('Template caricato!')
      setShowNew(false)
      load()
    } catch { toast.error('Errore') }
  }

  const activate = async (id) => {
    await plansAPI.activate(id)
    toast.success('Scheda attivata')
    load()
  }

  const deletePlan = async (id) => {
    if (!confirm('Eliminare questa scheda?')) return
    await plansAPI.delete(id)
    toast.success('Scheda eliminata')
    setDetail(null)
    load()
  }

  const openDetail = (plan) => {
    plansAPI.get(plan.id).then(r => setDetail(r.data.data)).catch(() => {})
  }

  return (
    <div>
      <div className="page-header">
        <span className="page-title">Schede</span>
        <button className="btn btn-sm" onClick={() => setShowNew(true)}>+ Nuova</button>
      </div>

      {plans.length === 0 ? (
        <div className="empty">
          <div className="empty-icon">📋</div>
          <div className="empty-text">Nessuna scheda creata.<br />Premi + Nuova per iniziare.</div>
        </div>
      ) : (
        plans.map(p => (
          <div key={p.id} className={`workout-item${p.isActive ? ' active-plan' : ''}`} onClick={() => openDetail(p)}>
            <div className="workout-emoji">{getSplitEmoji(p.split)}</div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div className="workout-name">{p.name}</div>
              <div className="workout-meta">{p.split} · {p.daysPerWeek} gg/sett · {p.goal}</div>
            </div>
            {p.isActive
              ? <span className="badge badge-green">Attiva</span>
              : <button className="btn btn-ghost btn-sm" onClick={e => { e.stopPropagation(); activate(p.id) }}>Usa</button>
            }
          </div>
        ))
      )}

      {/* MODAL new plan */}
      {showNew && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setShowNew(false)}>
          <div className="modal">
            <div className="modal-handle" />
            <div className="modal-title">Nuova Scheda</div>
            <form onSubmit={createPlan}>
              <div className="field"><label>Nome</label>
                <input required value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} placeholder="es. Push/Pull/Legs" />
              </div>
              <div className="row2">
                <div className="field"><label>Split</label>
                  <select value={form.split} onChange={e => setForm({ ...form, split: e.target.value })}>
                    {['Push/Pull/Legs','Full Body','Upper/Lower','Bro Split','Custom'].map(s => <option key={s}>{s}</option>)}
                  </select>
                </div>
                <div className="field"><label>Giorni/sett</label>
                  <select value={form.daysPerWeek} onChange={e => setForm({ ...form, daysPerWeek: e.target.value })}>
                    {[3,4,5,6].map(d => <option key={d}>{d}</option>)}
                  </select>
                </div>
              </div>
              <div className="field"><label>Obiettivo</label>
                <select value={form.goal} onChange={e => setForm({ ...form, goal: e.target.value })}>
                  {['Massa','Forza','Definizione','Salute'].map(g => <option key={g}>{g}</option>)}
                </select>
              </div>
              <div style={{ display: 'flex', gap: 8, marginTop: 4 }}>
                <button type="submit" className="btn" style={{ flex: 1 }}>Crea</button>
                <button type="button" className="btn btn-ghost" onClick={() => setShowNew(false)}>Annulla</button>
              </div>
            </form>
            <div className="section-label" style={{ marginTop: 20, padding: 0, marginBottom: 8 }}>Template Rapidi</div>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              {Object.keys(TEMPLATES).map(k => (
                <button key={k} className="btn btn-ghost btn-sm" onClick={() => loadTemplate(k)}>
                  {k === 'PPL' ? '💪 PPL 6gg' : k === '5x5' ? '🏋️ 5x5 Forza' : '⭐ Full Body 3gg'}
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* MODAL add exercise — z-index superiore al modal detail */}
      {showAddEx && (
        <div className="modal-overlay" style={{ zIndex: 300 }} onClick={e => e.target === e.currentTarget && setShowAddEx(null)}>
          <div className="modal">
            <div className="modal-handle" />
            <div className="modal-title">Aggiungi Esercizio</div>

            {!showCustomEx ? (
              <>
                {/* Ricerca */}
                <input
                  placeholder="Cerca esercizio…"
                  value={exSearch}
                  autoFocus
                  onChange={e => { setExSearch(e.target.value); setSelectedEx(null) }}
                  style={{ marginBottom: 8 }}
                />

                {/* Filtro muscolo */}
                <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginBottom: 6 }}>
                  {['Tutti','Petto','Dorsali','Spalle','Bicipiti','Tricipiti','Quadricipiti','Femorali','Glutei','Core','Polpacci'].map(m => (
                    <button
                      key={m}
                      className={`btn btn-sm ${exMuscleFilter === m ? '' : 'btn-ghost'}`}
                      style={{ padding: '3px 10px', fontSize: 12 }}
                      onClick={() => { setExMuscleFilter(m); setSelectedEx(null) }}
                    >{m}</button>
                  ))}
                </div>

                {/* Filtro categoria */}
                <div style={{ display: 'flex', gap: 6, marginBottom: 10 }}>
                  {['Tutti','Compound','Isolation','Core','Cardio'].map(c => (
                    <button
                      key={c}
                      className={`btn btn-sm ${exCatFilter === c ? '' : 'btn-ghost'}`}
                      style={{ padding: '3px 10px', fontSize: 11 }}
                      onClick={() => { setExCatFilter(c); setSelectedEx(null) }}
                    >{c}</button>
                  ))}
                </div>

                {/* Lista */}
                {!selectedEx && (
                  <div style={{ maxHeight: 220, overflowY: 'auto', marginBottom: 12, border: '1px solid var(--border)', borderRadius: 8 }}>
                    {exercises
                      .filter(ex =>
                        ex.name.toLowerCase().includes(exSearch.toLowerCase()) &&
                        (exMuscleFilter === 'Tutti' || ex.muscleGroup === exMuscleFilter) &&
                        (exCatFilter === 'Tutti' || ex.category === exCatFilter)
                      )
                      .map(ex => (
                        <div
                          key={ex.id}
                          onClick={() => { setSelectedEx(ex); setExSearch(ex.name) }}
                          style={{ padding: '10px 12px', cursor: 'pointer', borderBottom: '1px solid var(--border)', fontSize: 14 }}
                        >
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <span style={{ fontWeight: 500 }}>{ex.name}</span>
                            <span style={{ color: 'var(--text3)', fontSize: 11 }}>{ex.muscleGroup} · {ex.category}</span>
                          </div>
                          {ex.notes && (
                            <div style={{ fontSize: 11, color: 'var(--text3)', marginTop: 2, lineHeight: 1.4 }}
                                 onClick={e => e.stopPropagation()}>
                              {ex.notes.length > 80 ? ex.notes.slice(0, 80) + '…' : ex.notes}
                            </div>
                          )}
                        </div>
                      ))}
                  </div>
                )}

                {/* Esercizio selezionato */}
                {selectedEx && (
                  <div style={{ background: 'var(--bg3)', borderRadius: 8, marginBottom: 12, overflow: 'hidden' }}>
                    <div style={{ padding: '10px 12px', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div>
                        <div style={{ fontWeight: 600, fontSize: 14 }}>{selectedEx.name}</div>
                        <div style={{ fontSize: 12, color: 'var(--text3)' }}>{selectedEx.muscleGroup} · {selectedEx.category}</div>
                      </div>
                      <button className="btn btn-ghost btn-sm" onClick={() => { setSelectedEx(null); setExSearch('') }}>✕</button>
                    </div>
                    {selectedEx.notes && (
                      <div style={{ padding: '0 12px 10px', fontSize: 12, color: 'var(--text2)', lineHeight: 1.5, borderTop: '1px solid var(--border)' }}>
                        {selectedEx.notes}
                      </div>
                    )}
                  </div>
                )}

                {/* Parametri */}
                {selectedEx && (
                  <>
                    <div className="row2">
                      <div className="field"><label>Serie</label>
                        <input type="number" min={1} max={20} value={exForm.sets}
                          onChange={e => setExForm({ ...exForm, sets: parseInt(e.target.value) || 1 })} />
                      </div>
                      <div className="field"><label>Ripetizioni</label>
                        <input value={exForm.repsRange}
                          onChange={e => setExForm({ ...exForm, repsRange: e.target.value })}
                          placeholder="8-12" />
                      </div>
                    </div>
                    <div className="row2">
                      <div className="field"><label>Recupero (sec)</label>
                        <input type="number" min={0} value={exForm.restSeconds}
                          onChange={e => setExForm({ ...exForm, restSeconds: parseInt(e.target.value) || 0 })} />
                      </div>
                      <div className="field"><label>Peso target (kg)</label>
                        <input type="number" min={0} step={2.5} value={exForm.targetWeightKg}
                          onChange={e => setExForm({ ...exForm, targetWeightKg: e.target.value })}
                          placeholder="Facoltativo" />
                      </div>
                    </div>
                    <div className="field"><label>Note personali (facoltativo)</label>
                      <textarea
                        value={exForm.notes}
                        onChange={e => setExForm({ ...exForm, notes: e.target.value })}
                        placeholder="es. Concentrati sulla fase eccentrica, usa presa larga…"
                        rows={2}
                        style={{ resize: 'vertical' }}
                      />
                    </div>
                    <div style={{ display: 'flex', gap: 8, marginTop: 4 }}>
                      <button className="btn" style={{ flex: 1 }} onClick={async () => {
                        try {
                          const res = await plansAPI.addExercise(showAddEx.planId, showAddEx.dayId, {
                            exerciseId: selectedEx.id,
                            sets: exForm.sets,
                            repsRange: exForm.repsRange,
                            restSeconds: exForm.restSeconds,
                            targetWeightKg: exForm.targetWeightKg !== '' ? parseFloat(exForm.targetWeightKg) : null,
                            notes: exForm.notes || null,
                          })
                          setDetail(res.data.data)
                          setShowAddEx(null)
                          toast.success('Esercizio aggiunto!')
                        } catch { toast.error('Errore') }
                      }}>Aggiungi</button>
                      <button className="btn btn-ghost" onClick={() => setShowAddEx(null)}>Annulla</button>
                    </div>
                  </>
                )}

                <button
                  className="btn btn-ghost btn-sm"
                  style={{ marginTop: 12, width: '100%' }}
                  onClick={() => { setShowCustomEx(true); setCustomExForm({ name: exSearch, muscleGroup: 'Petto', category: 'Compound', notes: '' }) }}
                >
                  + Crea esercizio personalizzato
                </button>
              </>
            ) : (
              <>
                <div style={{ fontSize: 13, color: 'var(--text2)', marginBottom: 12 }}>Nuovo esercizio custom</div>
                <div className="field"><label>Nome</label>
                  <input autoFocus value={customExForm.name}
                    onChange={e => setCustomExForm({ ...customExForm, name: e.target.value })}
                    placeholder="es. Curl con Cavo Basso" />
                </div>
                <div className="row2">
                  <div className="field"><label>Muscolo</label>
                    <select value={customExForm.muscleGroup} onChange={e => setCustomExForm({ ...customExForm, muscleGroup: e.target.value })}>
                      {['Quadricipiti','Femorali','Glutei','Petto','Dorsali','Spalle','Bicipiti','Tricipiti','Core','Polpacci'].map(m => <option key={m}>{m}</option>)}
                    </select>
                  </div>
                  <div className="field"><label>Tipo</label>
                    <select value={customExForm.category} onChange={e => setCustomExForm({ ...customExForm, category: e.target.value })}>
                      {['Compound','Isolation','Core','Cardio'].map(c => <option key={c}>{c}</option>)}
                    </select>
                  </div>
                </div>
                <div className="field"><label>Note tecniche (facoltativo)</label>
                  <textarea
                    value={customExForm.notes}
                    onChange={e => setCustomExForm({ ...customExForm, notes: e.target.value })}
                    placeholder="Suggerimenti di esecuzione, muscoli coinvolti, varianti…"
                    rows={3}
                    style={{ resize: 'vertical' }}
                  />
                </div>
                <div style={{ display: 'flex', gap: 8, marginTop: 4 }}>
                  <button className="btn" style={{ flex: 1 }} onClick={async () => {
                    if (!customExForm.name.trim()) return
                    try {
                      const res = await exercisesAPI.create(customExForm)
                      const created = res.data.data
                      setExercises(prev => [...prev, created])
                      setSelectedEx(created)
                      setExSearch(created.name)
                      setShowCustomEx(false)
                      toast.success('Esercizio creato!')
                    } catch { toast.error('Errore nella creazione') }
                  }}>Crea</button>
                  <button className="btn btn-ghost" onClick={() => setShowCustomEx(false)}>Indietro</button>
                </div>
              </>
            )}
          </div>
        </div>
      )}

      {/* MODAL plan detail */}
      {detail && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setDetail(null)}>
          <div className="modal">
            <div className="modal-handle" />
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
              <div className="modal-title" style={{ margin: 0 }}>{detail.name}</div>
              <span className="badge badge-accent">{detail.goal}</span>
            </div>
            <div style={{ fontSize: 13, color: 'var(--text2)', marginBottom: 16 }}>
              {detail.split} · {detail.daysPerWeek} giorni/settimana
            </div>

            {(detail.workoutDays || []).map((day, di) => (
              <div key={day.id} style={{ marginBottom: 16 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                  <div style={{ fontSize: 14, fontWeight: 600, color: 'var(--accent2)' }}>{day.name}</div>
                  <button className="btn btn-ghost btn-sm" onClick={() => {
                    setShowAddEx({ planId: detail.id, dayId: day.id })
                    setSelectedEx(null)
                    setExSearch('')
                    setExMuscleFilter('Tutti')
                    setExCatFilter('Tutti')
                    setExForm({ sets: 3, repsRange: '8-12', restSeconds: 90, targetWeightKg: '', notes: '' })
                    setShowCustomEx(false)
                  }}>
                    + Esercizio
                  </button>
                </div>
                <div className="card" style={{ margin: 0 }}>
                  {day.exercises?.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '12px 0', color: 'var(--text3)', fontSize: 13 }}>
                      Nessun esercizio — aggiungi il primo
                    </div>
                  ) : (
                    day.exercises?.map(ex => (
                      <div key={ex.id} style={{ padding: '10px 0', borderBottom: '1px solid var(--border)' }}>
                        <div style={{ display: 'flex', alignItems: 'center' }}>
                          <div className="muscle-dot" style={{ background: MUSCLE_COLORS[ex.muscleGroup] || '#888', marginRight: 10 }} />
                          <div style={{ flex: 1 }}>
                            <div style={{ fontSize: 14, fontWeight: 500 }}>{ex.exerciseName}</div>
                            <div style={{ fontSize: 11, color: 'var(--text3)' }}>{ex.muscleGroup}</div>
                          </div>
                          <div style={{ fontSize: 12, color: 'var(--text2)', textAlign: 'right', marginRight: 8 }}>
                            {ex.sets}×{ex.repsRange}<br />
                            {ex.targetWeightKg > 0 ? `${ex.targetWeightKg}kg` : ''}
                          </div>
                          <button
                            className="btn btn-ghost btn-sm"
                            style={{ padding: '4px 8px', fontSize: 13 }}
                            onClick={() => {
                              setEditEx({ planId: detail.id, dayId: day.id, ex })
                              setEditForm({
                                sets: ex.sets,
                                repsRange: ex.repsRange,
                                restSeconds: ex.restSeconds,
                                targetWeightKg: ex.targetWeightKg > 0 ? ex.targetWeightKg : '',
                                notes: ex.notes || '',
                              })
                            }}
                          >✏️</button>
                        </div>
                        {ex.notes && (
                          <div style={{ fontSize: 12, color: 'var(--text2)', marginTop: 4, marginLeft: 22, lineHeight: 1.4, fontStyle: 'italic' }}>
                            {ex.notes}
                          </div>
                        )}
                      </div>
                    ))
                  )}
                </div>
              </div>
            ))}

            <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
              <button className="btn btn-danger btn-sm" onClick={() => deletePlan(detail.id)}>🗑 Elimina</button>
              {!detail.isActive && (
                <button className="btn btn-success" style={{ flex: 1 }} onClick={() => { activate(detail.id); setDetail(null) }}>
                  ✓ Usa questa scheda
                </button>
              )}
            </div>
          </div>
        </div>
      )}
      {/* MODAL edit exercise */}
      {editEx && (
        <div className="modal-overlay" style={{ zIndex: 400 }} onClick={e => e.target === e.currentTarget && setEditEx(null)}>
          <div className="modal">
            <div className="modal-handle" />
            <div className="modal-title">Modifica Esercizio</div>
            <div style={{ fontSize: 13, color: 'var(--text2)', marginBottom: 12 }}>{editEx.ex.exerciseName}</div>
            <div className="row2">
              <div className="field"><label>Serie</label>
                <input type="number" min={1} max={20} value={editForm.sets}
                  onChange={e => setEditForm({ ...editForm, sets: parseInt(e.target.value) || 1 })} />
              </div>
              <div className="field"><label>Ripetizioni</label>
                <input value={editForm.repsRange}
                  onChange={e => setEditForm({ ...editForm, repsRange: e.target.value })}
                  placeholder="8-12" />
              </div>
            </div>
            <div className="row2">
              <div className="field"><label>Recupero (sec)</label>
                <input type="number" min={0} value={editForm.restSeconds}
                  onChange={e => setEditForm({ ...editForm, restSeconds: parseInt(e.target.value) || 0 })} />
              </div>
              <div className="field"><label>Peso target (kg)</label>
                <input type="number" min={0} step={2.5} value={editForm.targetWeightKg}
                  onChange={e => setEditForm({ ...editForm, targetWeightKg: e.target.value })}
                  placeholder="Facoltativo" />
              </div>
            </div>
            <div className="field"><label>Note personali (facoltativo)</label>
              <textarea
                value={editForm.notes}
                onChange={e => setEditForm({ ...editForm, notes: e.target.value })}
                placeholder="es. Concentrati sulla fase eccentrica…"
                rows={2}
                style={{ resize: 'vertical' }}
              />
            </div>
            <div style={{ display: 'flex', gap: 8, marginTop: 4 }}>
              <button className="btn" style={{ flex: 1 }} onClick={async () => {
                try {
                  const res = await plansAPI.updateExercise(editEx.planId, editEx.dayId, editEx.ex.id, {
                    sets: editForm.sets,
                    repsRange: editForm.repsRange,
                    restSeconds: editForm.restSeconds,
                    targetWeightKg: editForm.targetWeightKg !== '' ? parseFloat(editForm.targetWeightKg) : null,
                    notes: editForm.notes || null,
                  })
                  setDetail(res.data.data)
                  setEditEx(null)
                  toast.success('Esercizio aggiornato!')
                } catch { toast.error('Errore') }
              }}>Salva</button>
              <button className="btn btn-ghost" onClick={() => setEditEx(null)}>Annulla</button>
            </div>
            <button className="btn btn-danger btn-sm" style={{ width: '100%', marginTop: 8 }} onClick={async () => {
              if (!confirm(`Rimuovere ${editEx.ex.exerciseName} dalla scheda?`)) return
              try {
                const res = await plansAPI.deleteExercise(editEx.planId, editEx.dayId, editEx.ex.id)
                setDetail(res.data.data)
                setEditEx(null)
                toast.success('Esercizio rimosso')
              } catch { toast.error('Errore') }
            }}>🗑 Rimuovi dalla scheda</button>
          </div>
        </div>
      )}
    </div>
  )
}

function generateDays(split, n) {
  const map = {
    'Push/Pull/Legs': ['Push','Pull','Legs'],
    'Full Body': Array.from({ length: n }, (_, i) => `Full Body ${i + 1}`),
    'Upper/Lower': ['Upper','Lower'],
    'Bro Split': ['Petto','Schiena','Spalle','Braccia','Gambe'],
    'Custom': Array.from({ length: n }, (_, i) => `Giorno ${i + 1}`),
  }
  const names = map[split] ?? Array.from({ length: n }, (_, i) => `Giorno ${i + 1}`)
  return names.slice(0, n).map((name, i) => ({ name, dayOrder: i, exercises: [] }))
}

function getSplitEmoji(split) {
  if (!split) return '💪'
  if (split.includes('Push')) return '🫸'
  if (split.includes('Full')) return '🌟'
  if (split.includes('Upper')) return '💪'
  if (split.includes('Bro')) return '🏋️'
  return '⚡'
}
