import { useState, createContext, useContext } from 'react'
import { Context } from '../App'
import Option from '../components/option'
import Search from '../components/search'
import Menu from './menu'

function AddItem() {
  const { page, setPage } = useContext(Context)

  return (
    <>
      <div className="h-[5vh] text-center" onClick={() => setPage(<Menu/>)}>Yeet</div>
    </>
  );
}

export default AddItem;
