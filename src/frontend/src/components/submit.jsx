function Submit(props) {

  return (
    <>
      <div className="w-full flex gap-[2%] justify-center">
        <div onClick={() => props.onClick()} className="
          h-[5vh] w-[70%] bg-[#22BB88] rounded-[5px] mb-[5vh] px-[2%] [cursor:pointer]
          text-white text-[3vh] text-center
        ">
          Submit
        </div>
      </div>
    </>
  )
}

export default Submit;