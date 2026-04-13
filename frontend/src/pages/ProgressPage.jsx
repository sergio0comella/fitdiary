import { useEffect, useState } from 'react'
import { statsAPI, exercisesAPI } from '../services/api'
import { Line, Bar } from 'react-chartjs-2'
import {
  Chart as ChartJS, CategoryScale, LinearScale, PointElement,
  LineElement, BarElement, Title, Tooltip, Legend, Filler
} from 'chart.js'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement,
  BarElement, Title, Tooltip, Legend, Filler)

const CHART_OPTS = {
  responsive: true,
  plugins: { legend: { display: false } },
  scales: {
    x: { ticks: { color: '#555', font: { size: 10 } }, grid: { color: 'rgba(255,255,255,0.04)' } },
    y: { ticks: { color: '#555', font: { size: 10 } }, grid: { color: 'rgba(255,255,255,0.04)' } }
  }
}

export default function ProgressPage() {
  const [tab, setTab] = useState('charts')
  const [volumeData, setVolumeData] = useState([])
  const [prs, setPrs] = useState([])
  const [exercises, setExercises] = useState([])
  const [selectedEx, setSelectedEx] = useState('')
  const [exProgress, setExProgress] = useState([])

  useEffect(() => {
    statsAPI.getVolume(12).then(r => setVolumeData(r.data.data)).catch(() => {})
    statsAPI.getPRs().then(r => setPrs(r.data.data)).catch(() => {})
    exercisesAPI.getAll().then(r => setExercises(r.data.data)).catch(() => {})
  }, [])

  useEffect(() => {
    if (selectedEx) {
      statsAPI.getExerciseProgress(selectedEx).then(r => setExProgress(r.data.data)).catch(() => {})
    }
  }, [selectedEx])

  const volChartData = {
    labels: volumeData.map(d => d.week),
    datasets: [{
      data: volumeData.map(d => Math.round(d.volumeKg / 1000)),
      backgroundColor: 'rgba(108,99,255,0.5)',
      borderColor: 'rgba(108,99,255,1)',
      borderWidth: 1,
      borderRadius: 4,
    }]
  }

  const exChartData = {
    labels: exProgress.map(d => new Date(d.date).toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit' })),
    datasets: [{
      data: exProgress.map(d => parseFloat(d.maxWeightKg)),
      borderColor: '#22c55e',
      backgroundColor: 'rgba(34,197,94,0.1)',
      tension: 0.3,
      pointRadius: 4,
      pointBackgroundColor: '#22c55e',
      fill: true,
    }]
  }

  return (
    <div>
      <div className="page-header">
        <span className="page-title">Progressi</span>
        <div className="tabs">
          <button className={`tab${tab === 'charts' ? ' active' : ''}`} onClick={() => setTab('charts')}>Grafici</button>
          <button className={`tab${tab === 'prs' ? ' active' : ''}`} onClick={() => setTab('prs')}>PR</button>
        </div>
      </div>

      {tab === 'charts' && (
        <>
          <div className="chart-wrap">
            <div className="chart-title">Volume Settimanale (×1000 kg)</div>
            {volumeData.length > 0
              ? <Bar data={volChartData} options={CHART_OPTS} height={140} />
              : <div style={{ textAlign: 'center', padding: 20, color: 'var(--text3)' }}>Nessun dato</div>
            }
          </div>

          <div className="card">
            <div className="card-title" style={{ marginBottom: 10 }}>Progressione Esercizio</div>
            <div className="field" style={{ marginBottom: 12 }}>
              <select value={selectedEx} onChange={e => setSelectedEx(e.target.value)}>
                <option value="">— Seleziona esercizio —</option>
                {exercises.map(ex => <option key={ex.id} value={ex.id}>{ex.name}</option>)}
              </select>
            </div>
            {exProgress.length > 0
              ? <Line data={exChartData} options={{ ...CHART_OPTS, scales: { ...CHART_OPTS.scales, y: { ...CHART_OPTS.scales.y, title: { display: true, text: 'kg', color: '#555' } } } }} height={140} />
              : <div style={{ textAlign: 'center', padding: 16, color: 'var(--text3)', fontSize: 13 }}>
                  {selectedEx ? 'Nessun dato per questo esercizio' : 'Seleziona un esercizio'}
                </div>
            }
          </div>
        </>
      )}

      {tab === 'prs' && (
        <div className="card">
          {prs.length === 0 ? (
            <div className="empty" style={{ padding: '32px 0' }}>
              <div className="empty-icon">🏆</div>
              <div className="empty-text">Nessun PR ancora.<br />Inizia ad allenarti!</div>
            </div>
          ) : (
            <div>
              {prs.map((pr, i) => (
                <div key={pr.exerciseId} className="pr-item">
                  <div className="pr-rank">{i + 1}</div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 14, fontWeight: 500 }}>{pr.exerciseName}</div>
                    <div style={{ fontSize: 12, color: 'var(--text2)', marginTop: 1 }}>
                      {parseFloat(pr.weightKg)}kg × {pr.reps} rep · {new Date(pr.achievedAt).toLocaleDateString('it-IT')}
                    </div>
                  </div>
                  <div className="pr-kg">
                    {parseFloat(pr.estimated1rm).toFixed(1)}kg
                    <div style={{ fontSize: 10, color: 'var(--text3)', fontWeight: 400 }}>1RM est.</div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      <div style={{ height: 8 }} />
    </div>
  )
}
