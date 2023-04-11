import Option from '../components/option'
import Search from '../components/search'
import { useState, createContext, useContext } from 'react'
import { Context } from '../App'

function Items() {
  const { page, setPage } = useContext(Context)

  return (
    <>
      <div className="h-[5vh] text-center" onClick={() => setPage(<AddItem/>)}>Title</div>

      <Search/>

      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>

      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>
      <Option name="Screws" stock={10}/>

      <div className="flex gap-[2vw] place-content-center place-items-center pb-[5vw]">
        Page {1} of {10} 
        <div className="
          h-[5vh] w-[5vw] bg-[#2288BB] rounded-[5px]
          text-white text-[4vh] leading-none text-center [cursor:pointer]
        ">
          {'>'}
        </div>
      </div>
    </>
  );
}

export default Items;
