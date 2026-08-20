import { useEffect, useRef, useState } from 'react'

const slides = [
  { index: '01', title: 'Projetos', text: 'Em breve, seus projetos aparecerão aqui.' },
  { index: '02', title: 'Tarefas', text: 'Organize o próximo passo de cada entrega.' },
  { index: '03', title: 'Ritmo', text: 'Acompanhe o trabalho sem perder o contexto.' },
]

export function FeatureCarousel() {
  const [activeIndex, setActiveIndex] = useState(0)
  const touchStart = useRef<number | null>(null)

  useEffect(() => {
    const timer = window.setInterval(() => setActiveIndex((index) => (index + 1) % slides.length), 5000)
    return () => window.clearInterval(timer)
  }, [])

  const move = (direction: number) => setActiveIndex((index) => (index + direction + slides.length) % slides.length)
  const slide = slides[activeIndex]

  return <section className="feature-carousel" aria-label="Destaques do workspace" onTouchStart={(event) => { touchStart.current = event.touches[0].clientX }} onTouchEnd={(event) => { if (touchStart.current === null) return; const distance = event.changedTouches[0].clientX - touchStart.current; if (Math.abs(distance) > 40) move(distance < 0 ? 1 : -1); touchStart.current = null }}><article className="carousel-slide accent-item"><span className="feature-index">{slide.index}</span><h2>{slide.title}</h2><p>{slide.text}</p></article><div className="carousel-controls"><button type="button" aria-label="Slide anterior" onClick={() => move(-1)}>←</button><div className="carousel-dots">{slides.map((item, index) => <button type="button" key={item.index} aria-label={`Ir para ${item.title}`} className={index === activeIndex ? 'active' : ''} onClick={() => setActiveIndex(index)} />)}</div><button type="button" aria-label="Próximo slide" onClick={() => move(1)}>→</button></div></section>
}