import Menu from './pages/menu'
import { useState, createContext } from 'react'

export const Context = createContext()

function App()
{
  const emptyPage = <></>
  const [page, setPage] = useState(emptyPage)

  if(page === emptyPage)
    setPage(<Menu/>)

  return (
    <>
      <main className="min-h-screen w-[100vw] bg-[#CCCCCC] pt-[5vh]">
        <Context.Provider value={{page, setPage}}>
          {page}
        </Context.Provider>
      </main>
    </>
  )
}

export default App;
