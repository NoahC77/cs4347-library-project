import { useState, useContext } from 'react'
import { Context } from '../App'

function Option(props) {
  const { page, setPage } = useContext(Context)

  return (
    <>
      <div className="flex gap-[2vw] mx-[15vw]" onClick={props.onClick}>
        <div className="
          h-[5vh] w-[70vw] bg-[#2288BB] rounded-[5px] mb-[5vh] px-[2vw] [cursor:pointer]
          text-white text-[3vh] grid grid-cols-2 gap-[2vw]
        ">
          <div className={props.className1}>{props.name}</div>
          <div className={props.className2}>{props.stock}</div>
        </div>
      </div>
    </>
  )
}

export default Option;